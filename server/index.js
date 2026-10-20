const express = require("express");
const bcrypt=require("bcryptjs");
const pool = require("./db");
const jwt = require("jsonwebtoken");



const app = express();
const PORT = 3000;
app.use(express.json());
function authenticateToken(request, response, next) {
    const authorizationHeader = request.headers.authorization;

    if (!authorizationHeader){
        return response.status(401).json({
         message: "Токен авторизации отсутствует"
        });
    }
    const [scheme, token] = authorizationHeader.split(" ");
    if (scheme !== "Bearer" || !token) {
        return response.status(401).json({
            message: "Неверный формат токена"
        });
    }
    try {
        const decoded = jwt.verify(
            token,
            process.env.JWT_SECRET,
            {              algorithms: ["HS256"],
                issuer: "activehabit-api"
            }
        );
        const userId = Number(decoded.userId);
         if (!Number.isSafeInteger(userId) || userId <= 0) {
             return response.status(401).json({
             message: "Токен содержит неверный идентификатор пользователя"
             });
         }//
         request.user = {
             userId: userId
         };
        next();
    } catch (error) {
        if (error.name === "TokenExpiredError") {
            return response.status(401).json({
                message: "Срок действия токена истёк"
            });
        }
        return response.status(401).json({
            message: "Недействительный токен"
        });
    }
}
app.get("/api/health", async (request, response) => {
    try {
        const result = await pool.query(
            "SELECT NOW() AS database_time"
        );
        response.json({
            status: "ok",
            server: "running",
            database: "connected",
            databaseTime: result.rows[0].database_time
        });
    } catch (error) {
        console.error("Database connection error:", error);
        response.status(500).json({
            status: "error",
            server: "running",
            database: "disconnected"
        });
    }
});
app.post("/api/auth/register", async (request, response) => {
    const { name, email, password } = request.body;

    if (
        typeof name !== "string" ||
        typeof email !== "string" ||
        typeof password !== "string"
    ) {
        return response.status(400).json({
            message: "Имя, email и пароль обязательны"
        });
    }
    const normalizedName = name.trim();
    const normalizedEmail = email.trim().toLowerCase();
    if (normalizedName.length < 2 || normalizedName.length > 100) {
        return response.status(400).json({
            message: "Имя должно содержать от 2 до 100 символов"
        });
    }
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(normalizedEmail)) {
        return response.status(400).json({
            message: "Некорректный email"
        });
    }
    const passwordPattern = /^[\x21-\x7E]{6,15}$/;
    if (!passwordPattern.test(password)) {
        return response.status(400).json({
            message: "Пароль должен содержать от 6 до 15 символов: латинские буквы, цифры и специальные символы без пробелов"
        });
    }
    try {
        const passwordHash = await bcrypt.hash(password, 10);
        const result = await pool.query(
            `INSERT INTO users (name, email, password_hash)
             VALUES ($1, $2, $3)
             RETURNING id, name, email, created_at`,
            [normalizedName, normalizedEmail, passwordHash]
        );
        return response.status(201).json({
            message: "Пользователь успешно зарегистрирован",
            user: result.rows[0]
        });
    } catch (error) {
        if (error.code === "23505") {
            return response.status(409).json({
                message: "Пользователь с таким email уже существует"
            });
        }
        console.error("Registration error:", error);
        return response.status(500).json({
            message: "Внутренняя ошибка сервера"
        });
    }
});
app.post("/api/auth/login", async (request, response) => {
    const { email, password } = request.body;
    if (
        typeof email !== "string" ||
        typeof password !== "string"
    ) {
        return response.status(400).json({
            message: "Email и пароль обязательны"
        });
    }
    const normalizedEmail = email.trim().toLowerCase();
    if (normalizedEmail.length === 0 || password.length === 0) {
        return response.status(400).json({
            message: "Email и пароль не могут быть пустыми"
        });
    }
    try {
        const result = await pool.query(
            `SELECT id, name, email, password_hash, created_at
             FROM users
             WHERE email = $1`,
            [normalizedEmail]
        );
        if (result.rows.length === 0) {
            return response.status(401).json({
                message: "Неверный email или пароль"
            });
        }
        const user = result.rows[0];
        const passwordMatches = await bcrypt.compare(
            password,
            user.password_hash
        );
        if (!passwordMatches) {
            return response.status(401).json({
                message: "Неверный email или пароль"
            });
        }
        const token= jwt.sign(
            {
                userId: Number(user.id)
            },
            process.env.JWT_SECRET,
            {
                expiresIn: "7d",
                issuer: "activehabit-api"
            }
        );
        return response.status(200).json({
            message: "Вход выполнен успешно",
            token: token,
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
                createdAt: user.created_at
            }
        });
    } catch (error) {
        console.error("Login error:", error);
        return response.status(500).json({
        message: "Ошибка сервера"
        });
    }
});
app.get("/api/profile", authenticateToken, async (request, response) => {
    try {
        const result = await pool.query(
            `SELECT id, name, email, created_at
             FROM users
             WHERE id = $1`,
            [request.user.userId]
        );

        if (result.rows.length === 0) {
            return response.status(404).json({
                message: "Пользователь не найден"
            });
        }

        const user = result.rows[0];

        return response.status(200).json({
            user: {
                id:user.id,
                name:user.name,
                email:user.email,
                createdAt:user.created_at
            }
        });
    } catch (error){
        console.error("Profile error:", error);
        return response.status(500).json({
            message: "Внутренняя ошибка сервера"
        });
    }
});
// Создание новой привычки
app.post("/api/habits", authenticateToken, async (request, response) => {
    try {
        const { title } = request.body;

        // Проверяем название привычки
        if (typeof title !== "string" || title.trim().length === 0) {
            return response.status(400).json({
                message: "Введите название привычки"
            });
        }

        if (title.trim().length > 100) {
            return response.status(400).json({
                message: "Название привычки слишком длинное"
            });
        }

        // Сохраняем привычку в PostgreSQL
        const result = await pool.query(
            `INSERT INTO habits (user_id, title)
             VALUES ($1, $2)
             RETURNING id, user_id, title, created_at`,
            [request.user.userId, title.trim()]
        );

        const habit = result.rows[0];

        return response.status(201).json({
            message: "Привычка успешно создана",
            habit: {
                id: habit.id,
                userId: habit.user_id,
                title: habit.title,
                createdAt: habit.created_at
            }
        });

    } catch (error) {
        console.error("Create habit error:", error);

        return response.status(500).json({
            message: "Внутренняя ошибка сервера"
        });
    }
});
// Получение привычек пользователя и подсчёт серии дней
app.get("/api/habits", authenticateToken, async (request, response) => {
    try {
        const result = await pool.query(
            `SELECT
                h.id,
                h.title,
                h.created_at,
                EXISTS (
                    SELECT 1
                    FROM habit_completions hc
                    WHERE hc.habit_id = h.id
                      AND hc.completed_date = CURRENT_DATE
                ) AS completed_today,
                (
                    SELECT COUNT(*)::integer
                    FROM (
                        SELECT
                            completed_date,
                            ROW_NUMBER() OVER (
                                ORDER BY completed_date DESC
                            ) AS day_number
                        FROM habit_completions
                        WHERE habit_id = h.id
                          AND completed_date <= CURRENT_DATE
                    ) AS days
                    WHERE completed_date =
                        CURRENT_DATE -
                        (
                            day_number::integer - 1 +
                            CASE
                                WHEN EXISTS (
                                    SELECT 1
                                    FROM habit_completions hc2
                                    WHERE hc2.habit_id = h.id
                                      AND hc2.completed_date = CURRENT_DATE
                                )
                                THEN 0
                                ELSE 1
                            END
                        )
                ) AS streak
             FROM habits h
             WHERE h.user_id = $1
             ORDER BY h.created_at DESC, h.id DESC`,
            [request.user.userId]
        );

        return response.status(200).json({
            habits: result.rows.map(habit => ({
                id: habit.id,
                title: habit.title,
                createdAt: habit.created_at,
                completedToday: habit.completed_today,
                streak: habit.streak
            }))
        });

    } catch (error) {
        console.error("Get habits error:", error);

        return response.status(500).json({
            message: "Внутренняя ошибка сервера"
        });
    }
});
// Отметить привычку выполненной за сегодня
app.post("/api/habits/:id/complete", authenticateToken, async (request, response) => {
    try {
        const habitId = Number(request.params.id);
        const userId = request.user.userId;

        if (!Number.isSafeInteger(habitId) || habitId <= 0) {
            return response.status(400).json({
                message: "Некорректный номер привычки"
            });
        }

        // Проверяем, что привычка принадлежит пользователю
        const habitResult = await pool.query(
            `SELECT id FROM habits
             WHERE id = $1 AND user_id = $2`,
            [habitId, userId]
        );

        if (habitResult.rows.length === 0) {
            return response.status(404).json({
                message: "Привычка не найдена"
            });
        }

        // Записываем выполнение за текущую дату сервера
        const result = await pool.query(
            `INSERT INTO habit_completions (habit_id, completed_date)
             VALUES ($1, CURRENT_DATE)
             ON CONFLICT (habit_id, completed_date)
             DO NOTHING
             RETURNING id, habit_id, completed_date`,
            [habitId]
        );

        return response.status(200).json({
            message: result.rows.length > 0
                ? "Привычка отмечена выполненной"
                : "Привычка уже выполнена сегодня"
        });

    } catch (error) {
        console.error("Complete habit error:", error);

        return response.status(500).json({
            message: "Внутренняя ошибка сервера"
        });
    }
});
// Удаление привычки пользователя
app.delete("/api/habits/:id", authenticateToken, async (request, response) => {
    try {
        const habitId = Number(request.params.id);
        const userId = request.user.userId;

        if (!Number.isSafeInteger(habitId) || habitId <= 0) {
            return response.status(400).json({
                message: "Некорректный номер привычки"
            });
        }

        const result = await pool.query(
            `DELETE FROM habits
             WHERE id = $1 AND user_id = $2
             RETURNING id`,
            [habitId, userId]
        );

        if (result.rows.length === 0) {
            return response.status(404).json({
                message: "Привычка не найдена"
            });
        }

        return response.status(200).json({
            message: "Привычка успешно удалена"
        });

    } catch (error) {
        console.error("Delete habit error:", error);

        return response.status(500).json({
            message: "Внутренняя ошибка сервера"
        });
    }
});
// Создание новой тренировки
app.post("/api/workouts", authenticateToken, async (request, response) => {
    try {
        const userId = request.user.userId;
        const { workoutType, durationMinutes, workoutDate } = request.body;

        if (
            typeof workoutType !== "string" ||
            workoutType.trim().length === 0 ||
            workoutType.trim().length > 100
        ) {
            return response.status(400).json({
                message: "Укажите корректный тип тренировки"
            });
        }

        if (
            !Number.isInteger(durationMinutes) ||
            durationMinutes <= 0
        ) {
            return response.status(400).json({
                message: "Продолжительность должна быть положительным числом минут"
            });
        }

        const result = await pool.query(
            `INSERT INTO workouts
                (user_id, workout_type, duration_minutes, workout_date)
             VALUES ($1, $2, $3, COALESCE($4::date, CURRENT_DATE))
             RETURNING id, workout_type, duration_minutes, workout_date`,
            [
                userId,
                workoutType.trim(),
                durationMinutes,
                workoutDate || null
            ]
        );

        return response.status(201).json({
            message: "Тренировка успешно добавлена",
            workout: result.rows[0]
        });

    } catch (error) {
        console.error("Create workout error:", error);

        return response.status(500).json({
            message: "Внутренняя ошибка сервера"
        });
    }
});
// Получение списка тренировок пользователя
app.get("/api/workouts", authenticateToken, async (request, response) => {
    try {
        const result = await pool.query(
            `SELECT
                id,
                workout_type,
                duration_minutes,
                workout_date,
                created_at
             FROM workouts
             WHERE user_id = $1
             ORDER BY workout_date DESC, id DESC`,
            [request.user.userId]
        );

        return response.status(200).json({
            workouts: result.rows.map(workout => ({
                id: workout.id,
                workoutType: workout.workout_type,
                durationMinutes: workout.duration_minutes,
                workoutDate: workout.workout_date,
                createdAt: workout.created_at
            }))
        });

    } catch (error) {
        console.error("Get workouts error:", error);

        return response.status(500).json({
            message: "Внутренняя ошибка сервера"
        });
    }
});
app.listen(PORT, () => {
    console.log(`ActiveHabit server started on port ${PORT}`);
});
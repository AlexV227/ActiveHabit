package com.activehabit.app

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.activehabit.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.nav_host_fragment
            ) as NavHostFragment

        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(
            R.navigation.nav_graph
        )

        val preferences = getSharedPreferences(
            "ActiveHabitPrefs",
            Context.MODE_PRIVATE
        )

        val token = preferences.getString(
            "auth_token",
            null
        )

        if (token.isNullOrEmpty()) {
            navGraph.setStartDestination(R.id.loginFragment)
        } else {
            navGraph.setStartDestination(R.id.homeFragment)
        }

        navController.graph = navGraph
    }
}
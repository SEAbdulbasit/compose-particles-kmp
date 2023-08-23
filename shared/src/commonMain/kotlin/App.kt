import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import clock.ComposeClock

@Composable
fun App() {
    MaterialTheme {
        ComposeClock()
    }
}

expect fun getPlatformName(): String
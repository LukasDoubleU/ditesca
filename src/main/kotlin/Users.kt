import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import java.io.File

object Users {

    private val path: String = System.getProperty("user.home") + File.separator + "ditesca.json"
    private val users = mutableListOf<User>()

    init {
        load()
    }

    fun load() {
        users.clear()
        val text = file().readText()
        if (text.isNotBlank())
            users.addAll(text.split("\n").map { JSON.parse<User>(it) })
    }

    fun save() {
        file().writeText(users.joinToString("\n") { JSON.stringify(it) })
    }

    private fun file(): File {
        val file = File(path)
        if (!file.exists()) file.createNewFile()
        return file
    }

    fun add(user: User) {
        users.add(user)
        save()
    }

    fun remove(user: User) {
        users.remove(user)
        save()
    }

    fun all() = users.toList()

}

@Serializable
data class User(val login: String, val password: String) {
    override fun toString() = login
}
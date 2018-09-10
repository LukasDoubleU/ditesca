import com.vaadin.flow.component.notification.Notification
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URL

object Vitesca {

    const val baseURL = "https://vitesca.de/kunden/wbestellung.php?"

    fun getBestellungenForUser(user: User): List<Bestellung> {

        // Baue die Verbindung auf und ermittle den Quelltext
        val doc = try {
            Jsoup.connect(URL("${baseURL}user=${user.login}&password=${user.password}").toString()).get()

        } catch (e: Exception) {
            Notification.show("Fehler beim Verbindungsaufbau mit Benutzer '$user'. Der Benutzer wird entfernt.")
            Users.remove(user)
            return emptyList()
        }

        val bestellungen = mutableListOf<Bestellung>()

        // Parse zunächst die Ungeraden Wochentage (Mo, Mi, Fr)
        val odd = doc.getElementsByClass("odd")
        odd.subList(0, 8).forEachIndexed { index, element -> bestellungen.plusAssign(toBestellung(user, element, "Montag", index)) }
        odd.subList(8, 16).forEachIndexed { index, element -> bestellungen.plusAssign(toBestellung(user, element, "Mittwoch", index)) }
        odd.subList(16, 24).forEachIndexed { index, element -> bestellungen.plusAssign(toBestellung(user, element, "Freitag", index)) }

        // Parse jetzt die Gerade Wochentage (Di, Do)
        val even = doc.getElementsByClass("even")
        even.subList(0, 8).forEachIndexed { index, element -> bestellungen.plusAssign(toBestellung(user, element, "Dienstag", index)) }
        even.subList(8, 16).forEachIndexed { index, element -> bestellungen.plusAssign(toBestellung(user, element, "Donnerstag", index)) }

        return bestellungen.filter { it.menge != 0 }.toList().sorted()
    }

    fun getBenutzerBestellungen(): List<BenutzerBestellung> {
        val perUser = Users.all().flatMap { getBestellungenForUser(it) }.groupBy { it.user }
        return perUser.map { entry -> BenutzerBestellung(entry.key, entry.value.find { it.tag == "Montag" }, entry.value.find { it.tag == "Dienstag" }, entry.value.find { it.tag == "Mittwoch" }, entry.value.find { it.tag == "Donnerstag" }, entry.value.find { it.tag == "Freitag" }) }
    }
}

private fun toBestellung(user: User, e: Element, tag: String, menu: Int) = Bestellung(
        user = user,
        text = e.ownText(),
        menge = e.getElementsByClass("menge")
                ?.first()
                ?.attr("value")
                ?.toIntOrNull()
                ?: 0,
        tag = tag,
        menu = menu
)

data class Bestellung(val user: User, val text: String, val menge: Int, val tag: String, val menu: Int) : Comparable<Bestellung> {
    override fun compareTo(other: Bestellung) = order().compareTo(other.order())

    private fun order() = when (tag) {
        "Montag" -> 0
        "Dienstag" -> 1
        "Mittwoch" -> 2
        "Donnerstag" -> 3
        "Freitag" -> 4
        else -> -1
    }

    override fun toString() = "M$menu: $text"
}

data class BenutzerBestellung(val name: User, val mo: Bestellung?, val di: Bestellung?, val mi: Bestellung?, val don: Bestellung?, val fr: Bestellung?) : Comparable<BenutzerBestellung> {
    override fun compareTo(other: BenutzerBestellung) = name.login.compareTo(other.name.login)
}
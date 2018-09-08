import com.github.vok.karibudsl.flow.*
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

@Theme(Lumo::class)
@Route("")
class UI : VerticalLayout() {

    val allUserLayout = AllUserLayout()
    val allUserTab = AllUserTab()
    val singleUserLayout = SingleUserLayout()
    val singleUserTab = SingleUserTab()
    val myTabs = MyTabs()

    init {
        val image = Image("Ditesca.png", "Ditesca-Logo")
        image.flexGrow = 0.0
        add(HorizontalLayout(myTabs, image))
        myTabs.flexGrow = 1.0
        setSizeFull()
        val div = Div(allUserLayout, singleUserLayout)
        div.setSizeFull()
        add(div)
    }

    inner class MyTabs : Tabs(allUserTab, singleUserTab) {

        private var previous: MyTab = selectedTab as MyTab

        init {
            addSelectedChangeListener {
                previous.onHide()
                (selectedTab as MyTab).onShow()
                previous = selectedTab as MyTab
            }
        }
    }

    open class MyTab(s: String, private val c: Component) : Tab(s) {

        fun onShow() {
            c.isVisible = true
        }

        fun onHide() {
            c.isVisible = false
        }
    }

    inner class AllUserTab : MyTab("Alle", allUserLayout)

    class AllUserLayout : VerticalLayout() {

        lateinit var grid: Grid<BenutzerBestellung>

        init {
            isVisible = true
            horizontalLayout {
                val name = textField("Name")
                val password = passwordField("Password")
                button(icon = VaadinIcon.PLUS_CIRCLE_O.create()) {
                    onLeftClick {
                        Users.add(User(name.value, password.value))
                        grid.setItems(Vitesca.getBenutzerBestellungen())
                    }
                }
            }
            grid = grid {
                addColumn(BenutzerBestellung::name).apply {
                    setHeader("Name")
                    flexGrow = 0
                    isFrozen = true
                }
                addColumn(BenutzerBestellung::mo).setHeader("Mo")
                addColumn(BenutzerBestellung::di).setHeader("Di")
                addColumn(BenutzerBestellung::mi).setHeader("Mi")
                addColumn(BenutzerBestellung::don).setHeader("Do")
                addColumn(BenutzerBestellung::fr).setHeader("Fr")
            }
            grid.setItems(Vitesca.getBenutzerBestellungen())
        }
    }

    inner class SingleUserTab : MyTab("Einer", singleUserLayout)

    inner class SingleUserLayout : VerticalLayout() {

        lateinit var grid: Grid<Bestellung>

        init {
            isVisible = false
            horizontalLayout {
                val name = textField("Name")
                val password = passwordField("Password")
                button(icon = VaadinIcon.REFRESH.create()) {
                    onLeftClick {
                        grid.setItems(Vitesca.getBestellungenForUser(User(name.value, password.value)))
                    }
                }
            }
            grid = grid {
                addColumn(Bestellung::tag).setHeader("Tag").flexGrow = 0
                addColumn(Bestellung::menu).setHeader("Menu").flexGrow = 0
                addColumn(Bestellung::text).setHeader("Text")
                addColumn(Bestellung::menge).setHeader("Menge").flexGrow = 0
                isColumnReorderingAllowed = true
            }
            grid.setItems(Vitesca.getBestellungenForUser(User("Wulff", "qweqweqwe")).sorted())
        }

    }
}

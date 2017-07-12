package models.embedded;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import helper.Webs;
import models.market.M;
import models.market.Selling;
import org.apache.http.NameValuePair;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.libs.F;
import play.libs.IO;
import play.test.UnitTest;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/6/13
 * Time: 11:53 AM
 */
public class AmazonPropsTest extends UnitTest {
    @Before
    public void setUP() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testGenerateDeployProps() throws IOException {
        String html = IO.readContentAsString(Play.getFile("test/models/market/B00DU8PP8G.html"), "utf-8");
        Selling selling = FactoryBoy.build(Selling.class, "de");
        F.T2<Collection<NameValuePair>, String> t2 = selling.aps.generateDeployProps(html, selling);
        assertThat(t2._1.size(), is(greaterThan(300)));
    }

    @Test
    public void testDeploy() throws IOException, ClassNotFoundException {
        Selling selling = FactoryBoy.build(Selling.class, "de", new BuildCallback<Selling>() {
            @Override
            public void build(Selling target) {
                target.merchantSKU = "73SMS4MINI-BVMGL,881165105706";
                target.asin = "B00EZJHUEQ";
//                target.market = M.AMAZON_DE;
                target.market = M.AMAZON_IT;
                AmazonProps aps = target.aps;
//                aps.title = "EasyAcc Schutzhülle für Kindle Paperwhite Case Leder Tasche hülle Mit Sleep / Wake up Funktion - Blau";
                aps.title = "EasyAcc Funda Inteligente para Kindle Paperwhite Funda (Cuero Sintético, azul)";
                aps.manufacturer = "EasyAcc";
                aps.brand = "EasyAcc";
                aps.manufacturerPartNumber = "Paperwhite";
                aps.rbns.clear();
                //de
//                aps.rbns.add("815150031");
//                aps.rbns.add("671887031");
                aps.rbns.add("937811031");
                //25.99, 19.99
                aps.standerPrice = 25.99f;
                aps.salePrice = 19.99f;
                aps.startDate = DateTime.parse("2013-05-19").toDate();
                aps.endDate = DateTime.parse("2015-05-16").toDate();
                aps.isGiftWrap = true;
                //de
                /*
                aps.productDesc = "<b style=\"color:#3399cc\">Material</b><br>\n" +
                        "<b>Außenseite</b>---Extrem haltbares Kunstleder. Sie müssen sich keine Sorgen wegen Verschleiß oder Kratzern machen. Authentische Lederoptik.<br>\n" +
                        "<b>Innenseite</b>--- Weiche Innenseite, das spezielle Material schützt und reinigt den Bildschirm.<br><br>\n" +
                        "<b style=\"color:#3399cc\">Maßgeschneidert</b><br>\n" +
                        "Die Kunstleder-Schutzhülle ist speziell für Kindle Paperwhite angefertigt. Der Rahmen sitzt eng und fest an Ihrem Gerät. Sämtliche Knöpfe und Anschlüsse wie der Netzanschluss, der Kopfhörer-Anschluss, Kamera und Lautsprecher sind ausgespart; Sie können Ihr Gerät benutzen ohne die Schutzhülle zu entfernen.<br><br>\n" +
                        "<b style=\"color:#3399cc\">Design</b><br>\n" +
                        "<b>Einfach zu tragen</b>---Dünne und leichte Schutzhülle. Ideal geeignet um Ihr Gerät in einer Tasche zu transportieren.<br>\n" +
                        "<b>Magnetischer Verschluss</b> - Der starke Magnetverschluss sorgt für einen sicheren halt der Lasche. Damit Ihr Gerät beim Transport im Rucksack oder in der Handtasche auch immer gut geschützt ist. Z.B. vor Erschütterungen und äußeren Einflüsen.<br>\n" +
                        "<b>Automatischer Ruhezustand und Aufwecken</b>---Versetzen Sie Ihr Gerät beim Schließen in den Ruhezustand und wecken Sie es beim Öffnen wieder auf. Dazu einfach nur den Deckel schließen und öffnen. Sie sparen damit mehr Energie und schützen den Einschaltknopf<br>\n" +
                        "<b>Magnetic closure</b>---Strong magnetic closure makes sure your lid closed well to protect your device while in a backpack, handbag or briefcase with shake";
                        */
                aps.productDesc =
                        "Exterior: sintético PU de cuero, con una buena resistencia al desgaste y tacto cómodo. <br>\n" +
                                "\n" +
                                "Interior: pelusa suave, con buena sensación del tacto, las salvaguardias y limpia la pantalla.<br>\n" +
                                "\n" +
                                "¡Una medida sólo para Amazon Kindle Paperwhite. Todos los puertos y botones cortados; Deja un acceso completo a todas las teclas / del puerto USB<br>\n" +
                                "\n" +
                                "Sueño auto y despierta función toma el dispositivo va a dormir una vez que se cierre y al instante despertándolo al abrirlo listo para su uso<br>\n" +
                                "\n" +
                                "\n" +
                                "Diseño funcional; corte aseado y punto gratuito de mano de obra exquisita, todos los puertos y botones de fácil acceso.<br>";

                aps.keyFeturess.clear();
                aps.searchTermss.clear();
                aps.keyFeturess
                        .add("Außenseite: Strapazierfähiges Kunstleder mit echter Lederoptik und hoher Haltbarkeit; Innenseite: Mikrofaser, angenehm weich, schützt und reinigt den Bildschirm effektiv");
                aps.keyFeturess
                        .add("Harte Hülle kann Ihr Gerät vor leichtem Stoß schützen. super dünne Hülle ist leichter zu tragen");
                aps.keyFeturess
                        .add("Speziell maßgeschneidert für Kindle Paperwhite. Präzise Aussparungen für alle Anschlüsse, Kontrollen, Sensoren und Kameras.");
                aps.keyFeturess
                        .add("Versetzt Ihr Gerät beim Schließen automatisch in den Ruhezustand und schaltet es beim Öffnen wieder ein");
                aps.keyFeturess
                        .add("Der starke Magnetverschluss sorgt für einen sicheren halt der Lasche. Damit Ihr Gerät beim Transport im Rucksack oder in der Handtasche auch immer gut geschützt ist. Z.B. vor Erschütterungen und äußeren Einflüsen.");
                aps.searchTermss.add("Leder Schutzhülle Hüllen Tasche Lederhülle");
                aps.searchTermss.add("Leather Case Cover Premium Leder Case book");
                aps.searchTermss.add("ForeFront Cases® Hülle für Kindle Paperwhite");
                aps.searchTermss.add("Swees UltraSlim Cover für Kindle Paperwhite");
                aps.searchTermss.add("grün rosa lila schwarz");

            }
        });
        Webs.devLogin(selling.account);
        selling.deploy();
    }
}

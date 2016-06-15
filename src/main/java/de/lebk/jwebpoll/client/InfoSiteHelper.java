package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.client.Dialogs.ConfirmDialog;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class InfoSiteHelper {
    private static final Logger LOGGER = Logger.getLogger(InfoSiteHelper.class);

    public static void show(String resource) {
        Stage evaluationStage = new Stage();
        evaluationStage.setTitle(resource);
        evaluationStage.getIcons().add(new Image(InfoSiteHelper.class.getResource("/icon.png").toString()));
        WebView webview;
        GridPane helpGrid;
        try {
            helpGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/helpSite.fxml"));
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("", ex);
            return;
        }


        webview = (WebView) helpGrid.lookup("#webview");
        String view = "<html><body><h2>Fehler (404 - Not Found)</h2> Bedauerlicher Weise konnte die Hilfeseite nicht geladen werden.<br>"
                + "Noch bedauerlicher ist es, dass es keine Hilfeseite gibt wenn man wissen will, warum die Hilfeseite nicht lädt.</body> </html>";

        switch (resource) {
            case "about" :
                view=getAbout();
                break;
            case "help" :
                view=getHelp();
                break;
            case "license" :
                view=getLicense();
                break;

        }
        webview.getEngine().loadContent(view);

        webview.setVisible(true);
        helpGrid.setVisible(true);
        evaluationStage.setScene(new Scene(helpGrid));
        evaluationStage.sizeToScene();
        evaluationStage.show();
    }


    private static String getAbout() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Über</title>\n" +
                "</head>\n" +
                "<body style='font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif'>\n" +
                "\n" +
                "<h2>Über JWebPoll</h2>\n" +
                "<p>JWebPoll ist eine webbasierende Offline-Umfragesoftware.</p>\n" +
                "<p>Umfragen können unkompliziert erstellt und über das lokale Netzwerk Anderen zur Abstimmung zugänglich gemacht\n" +
                "    werden.</p>\n" +
                "<br>\n" +
                "<p>Viel Spaß!</p>\n" +
                "<br>\n" +
                "<p>Euer JWebPoll Team.</p>\n" +
                "<p>Tim, Lucas, Felix, Simon, Florian</p>\n" +
                "<br>\n" +
                "<br>\n" +
                "<!-- Place this tag where you want the button to render. -->\n" +
                "<a class=\"github-button\" href=\"https://github.com/fgreinus/jwebpoll\" data-style=\"mega\"\n" +
                "   aria-label=\"Watch fgreinus/jwebpoll on GitHub\">Watch</a>\n" +
                "<!-- Place this tag in your head or just before your close body tag. -->\n" +
                "<script async defer src=\"https://buttons.github.io/buttons.js\"></script>\n" +
                "</body>\n" +
                "</html>";
    }

    private static String getHelp() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Hilfe</title>\n" +
                "</head>\n" +
                "<body style='font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif'>\n" +
                "\n" +
                "<h2>Umfragen anlegen und entfernen</h2>\n" +
                "Auf den Buttons im linken Teil des Fensters ganz unten können Umfragen hinzugefügt bzw. entfernt werden.\n" +
                "<h2>Fragen hinzufügen</h2>\n" +
                "Ist eine Umfrage ausgewählt können über den Button \"Hinzufügen\" Fragen hinzugefügt werden.\n" +
                "<h3>Frage</h3>\n" +
                "Bestimmt die eigentliche Frage. <i>Beispiel:</i> Wenn Sonntag Bundestagswahl wäre, wen würden sie wählen?\n" +
                "<h3>Hinweis</h3>\n" +
                "Kann die gestellte Frage präzisieren. <i>Beispiel:</i> Berücksichtigen sie aktuelle politische Ereignisse.\n" +
                "<h3>Pflichtfrage</h3>\n" +
                "Gibt an ob die Frage beantwortet sein muss um den Fragebogen abgeben zu können.\n" +
                "<h3>Fragentyp</h3>\n" +
                "Es gibt 3 Frage Typen:\n" +
                "<h4>Singlechoice</h4>\n" +
                "Es kann aus den gegebenen Antwortmöglichkeiten nur eine ausgewählt werden.\n" +
                "\n" +
                "<h4>Multiplechoice</h4>\n" +
                "Es können mehrere Antwortmöglichkeiten ausgewählt werden werden.\n" +
                "<h4>Freitext</h4>\n" +
                "Es kann ein beliebiger Text angegeben werden.\n" +
                "<h3>Antwortmöglichkeiten</h3>\n" +
                "<h4>Text</h4>\n" +
                "Die Antwort möglichekiten zu einer Frage. <i>Beispiel:</i> Wenn Sonntag Wahl wäre, wenn würden sie wählen? A: SPD B: CDU C:Piratenpartei\n" +
                "<h4>Wert</h4>\n" +
                "Gibt die Gewichtung einer Frage an. <i>Beispiel:</i> Es gibt einen Fragebogen für die Notenvergabe. Die Codequalität soll doppelt so stark gewichtet werden wie das Design.\n" +
                "<h2>Freigeben und schließen von Umfragen</h2>\n" +
                "<h3>Status</h3>\n" +
                "Der Status einer Umfrage\n" +
                "<h4>Neu</h4>\n" +
                "Die Umfrage wurde noch nie freigegeben.\n" +
                "<h4>Offen</h4>\n" +
                "Unter der Webadresse kann die Umfrage durchgeführt werden.\n" +
                "<h4>Geschlossen</h4>\n" +
                "Die Umfrage wurde geschlossen. Es kann nicht mehr abgestimmt werden.\n" +
                "<h3>Webadresse</h3>\n" +
                "Die Adresse unter der Geräte im Netzwerk die Umfragen aufrufen können.\n" +
                "Es kann immer nur eine Umfrage gleichzeitig geöffnet werden.\n" +
                "<h2>Ergebnisse und Auswertung</h2>\n" +
                "Der Button Ergebnisse öffnet die Auswertung. Es wird angezeigt, welche Antwortmöglichkeit wie oft gegeben wurde. Außerdem werden alle Antworten der Freitext Fragen gezeigt.\n" +
                "</body>\n" +
                "</html>";
    }

    private static String getLicense() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Lizenzen</title>\n" +
                "</head>\n" +
                "<body style='font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif'>\n" +
                "\n" +
                "sqlite-jdbc,spark-freemarker and spark core are under the Apache 2.0 License.<br><br>orm-lite is under this License:<br>Permission\n" +
                "to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby<br>granted, provided\n" +
                "that this permission notice appear in all copies.<br><br>THE SOFTWARE IS PROVIDED \"AS IS\" AND THE AUTHOR DISCLAIMS ALL\n" +
                "WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING<br>ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT\n" +
                "SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,<br>DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER\n" +
                "RESULTING FROM LOSS OF USE, DATA OR PROFITS,<br>WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,\n" +
                "ARISING OUT OF OR IN CONNECTION WITH THE<br>USE OR PERFORMANCE OF THIS SOFTWARE.<br><br>The author may be contacted via\n" +
                "http://ormlite.com/<br><br><br> Apache License<br> Version 2.0, January 2004<br> http://www.apache.org/licenses/<br><br>\n" +
                "TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION<br><br> 1. Definitions.<br><br> \"License\" shall mean the\n" +
                "terms and conditions for use, reproduction,<br> and distribution as defined by Sections 1 through 9 of this\n" +
                "document.<br><br> \"Licensor\" shall mean the copyright owner or entity authorized by<br> the copyright owner that is\n" +
                "granting the License.<br><br> \"Legal Entity\" shall mean the union of the acting entity and all<br> other entities that\n" +
                "control, are controlled by, or are under common<br> control with that entity. For the purposes of this definition,<br>\n" +
                "\"control\" means (i) the power, direct or indirect, to cause the<br> direction or management of such entity, whether by\n" +
                "contract or<br> otherwise, or (ii) ownership of fifty percent (50%) or more of the<br> outstanding shares, or (iii)\n" +
                "beneficial ownership of such entity.<br><br> \"You\" (or \"Your\") shall mean an individual or Legal Entity<br> exercising\n" +
                "permissions granted by this License.<br><br> \"Source\" form shall mean the preferred form for making modifications,<br>\n" +
                "including but not limited to software source code, documentation<br> source, and configuration files.<br><br> \"Object\"\n" +
                "form shall mean any form resulting from mechanical<br> transformation or translation of a Source form, including but<br>\n" +
                "not limited to compiled object code, generated documentation,<br> and conversions to other media types.<br><br> \"Work\"\n" +
                "shall mean the work of authorship, whether in Source or<br> Object form, made available under the License, as indicated\n" +
                "by a<br> copyright notice that is included in or attached to the work<br> (an example is provided in the Appendix\n" +
                "below).<br><br> \"Derivative Works\" shall mean any work, whether in Source or Object<br> form, that is based on (or\n" +
                "derived from) the Work and for which the<br> editorial revisions, annotations, elaborations, or other modifications<br>\n" +
                "represent, as a whole, an original work of authorship. For the purposes<br> of this License, Derivative Works shall not\n" +
                "include works that remain<br> separable from, or merely link (or bind by name) to the interfaces of,<br> the Work and\n" +
                "Derivative Works thereof.<br><br> \"Contribution\" shall mean any work of authorship, including<br> the original version\n" +
                "of the Work and any modifications or additions<br> to that Work or Derivative Works thereof, that is intentionally<br>\n" +
                "submitted to Licensor for inclusion in the Work by the copyright owner<br> or by an individual or Legal Entity\n" +
                "authorized to submit on behalf of<br> the copyright owner. For the purposes of this definition, \"submitted\"<br> means\n" +
                "any form of electronic, verbal, or written communication sent<br> to the Licensor or its representatives, including but\n" +
                "not limited to<br> communication on electronic mailing lists, source code control systems,<br> and issue tracking\n" +
                "systems that are managed by, or on behalf of, the<br> Licensor for the purpose of discussing and improving the Work, but<br>\n" +
                "excluding communication that is conspicuously marked or otherwise<br> designated in writing by the copyright owner as\n" +
                "\"Not a Contribution.\"<br><br> \"Contributor\" shall mean Licensor and any individual or Legal Entity<br> on behalf of whom\n" +
                "a Contribution has been received by Licensor and<br> subsequently incorporated within the Work.<br><br> 2. Grant of\n" +
                "Copyright License. Subject to the terms and conditions of<br> this License, each Contributor hereby grants to You a\n" +
                "perpetual,<br> worldwide, non-exclusive, no-charge, royalty-free, irrevocable<br> copyright license to reproduce,\n" +
                "prepare Derivative Works of,<br> publicly display, publicly perform, sublicense, and distribute the<br> Work and such\n" +
                "Derivative Works in Source or Object form.<br><br> 3. Grant of Patent License. Subject to the terms and conditions\n" +
                "of<br> this License, each Contributor hereby grants to You a perpetual,<br> worldwide, non-exclusive, no-charge,\n" +
                "royalty-free, irrevocable<br> (except as stated in this section) patent license to make, have made,<br> use, offer to\n" +
                "sell, sell, import, and otherwise transfer the Work,<br> where such license applies only to those patent claims\n" +
                "licensable<br> by such Contributor that are necessarily infringed by their<br> Contribution(s) alone or by combination\n" +
                "of their Contribution(s)<br> with the Work to which such Contribution(s) was submitted. If You<br> institute patent\n" +
                "litigation against any entity (including a<br> cross-claim or counterclaim in a lawsuit) alleging that the Work<br> or a\n" +
                "Contribution incorporated within the Work constitutes direct<br> or contributory patent infringement, then any patent\n" +
                "licenses<br> granted to You under this License for that Work shall terminate<br> as of the date such litigation is\n" +
                "filed.<br><br> 4. Redistribution. You may reproduce and distribute copies of the<br> Work or Derivative Works thereof in\n" +
                "any medium, with or without<br> modifications, and in Source or Object form, provided that You<br> meet the following\n" +
                "conditions:<br><br> (a) You must give any other recipients of the Work or<br> Derivative Works a copy of this License;\n" +
                "and<br><br> (b) You must cause any modified files to carry prominent notices<br> stating that You changed the files; and<br><br>\n" +
                "(c) You must retain, in the Source form of any Derivative Works<br> that You distribute, all copyright, patent,\n" +
                "trademark, and<br> attribution notices from the Source form of the Work,<br> excluding those notices that do not pertain\n" +
                "to any part of<br> the Derivative Works; and<br><br> (d) If the Work includes a \"NOTICE\" text file as part of its<br>\n" +
                "distribution, then any Derivative Works that You distribute must<br> include a readable copy of the attribution notices\n" +
                "contained<br> within such NOTICE file, excluding those notices that do not<br> pertain to any part of the Derivative\n" +
                "Works, in at least one<br> of the following places: within a NOTICE text file distributed<br> as part of the Derivative\n" +
                "Works; within the Source form or<br> documentation, if provided along with the Derivative Works; or,<br> within a\n" +
                "display generated by the Derivative Works, if and<br> wherever such third-party notices normally appear. The\n" +
                "contents<br> of the NOTICE file are for informational purposes only and<br> do not modify the License. You may add Your\n" +
                "own attribution<br> notices within Derivative Works that You distribute, alongside<br> or as an addendum to the NOTICE\n" +
                "text from the Work, provided<br> that such additional attribution notices cannot be construed<br> as modifying the\n" +
                "License.<br><br> You may add Your own copyright statement to Your modifications and<br> may provide additional or\n" +
                "different license terms and conditions<br> for use, reproduction, or distribution of Your modifications, or<br> for any\n" +
                "such Derivative Works as a whole, provided Your use,<br> reproduction, and distribution of the Work otherwise complies\n" +
                "with<br> the conditions stated in this License.<br><br> 5. Submission of Contributions. Unless You explicitly state\n" +
                "otherwise,<br> any Contribution intentionally submitted for inclusion in the Work<br> by You to the Licensor shall be\n" +
                "under the terms and conditions of<br> this License, without any additional terms or conditions.<br> Notwithstanding the\n" +
                "above, nothing herein shall supersede or modify<br> the terms of any separate license agreement you may have\n" +
                "executed<br> with Licensor regarding such Contributions.<br><br> 6. Trademarks. This License does not grant permission\n" +
                "to use the trade<br> names, trademarks, service marks, or product names of the Licensor,<br> except as required for\n" +
                "reasonable and customary use in describing the<br> origin of the Work and reproducing the content of the NOTICE\n" +
                "file.<br><br> 7. Disclaimer of Warranty. Unless required by applicable law or<br> agreed to in writing, Licensor\n" +
                "provides the Work (and each<br> Contributor provides its Contributions) on an \"AS IS\" BASIS,<br> WITHOUT WARRANTIES OR\n" +
                "CONDITIONS OF ANY KIND, either express or<br> implied, including, without limitation, any warranties or conditions<br>\n" +
                "of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A<br> PARTICULAR PURPOSE. You are solely responsible for\n" +
                "determining the<br> appropriateness of using or redistributing the Work and assume any<br> risks associated with Your\n" +
                "exercise of permissions under this License.<br><br> 8. Limitation of Liability. In no event and under no legal\n" +
                "theory,<br> whether in tort (including negligence), contract, or otherwise,<br> unless required by applicable law (such\n" +
                "as deliberate and grossly<br> negligent acts) or agreed to in writing, shall any Contributor be<br> liable to You for\n" +
                "damages, including any direct, indirect, special,<br> incidental, or consequential damages of any character arising as a<br>\n" +
                "result of this License or out of the use or inability to use the<br> Work (including but not limited to damages for loss\n" +
                "of goodwill,<br> work stoppage, computer failure or malfunction, or any and all<br> other commercial damages or losses),\n" +
                "even if such Contributor<br> has been advised of the possibility of such damages.<br><br> 9. Accepting Warranty or\n" +
                "Additional Liability. While redistributing<br> the Work or Derivative Works thereof, You may choose to offer,<br> and\n" +
                "charge a fee for, acceptance of support, warranty, indemnity,<br> or other liability obligations and/or rights\n" +
                "consistent with this<br> License. However, in accepting such obligations, You may act only<br> on Your own behalf and on\n" +
                "Your sole responsibility, not on behalf<br> of any other Contributor, and only if You agree to indemnify,<br> defend,\n" +
                "and hold each Contributor harmless for any liability<br> incurred by, or claims asserted against, such Contributor by\n" +
                "reason<br> of your accepting any such warranty or additional liability.<br><br> END OF TERMS AND CONDITIONS<br><br>\n" +
                "APPENDIX: How to apply the Apache License to your work.<br><br> To apply the Apache License to your work, attach the\n" +
                "following<br> boilerplate notice, with the fields enclosed by brackets \"[]\"<br> replaced with your own identifying\n" +
                "information. (Don't include<br> the brackets!) The text should be enclosed in the appropriate<br> comment syntax for the\n" +
                "file format. We also recommend that a<br> file or class name and description of purpose be included on the<br> same\n" +
                "\"printed page\" as the copyright notice for easier<br> identification within third-party archives.<br><br> Copyright\n" +
                "[yyyy] [name of copyright owner]<br><br> Licensed under the Apache License, Version 2.0 (the \"License\");<br> you may not\n" +
                "use this file except in compliance with the License.<br> You may obtain a copy of the License at<br><br>\n" +
                "http://www.apache.org/licenses/LICENSE-2.0<br><br> Unless required by applicable law or agreed to in writing,\n" +
                "software<br> distributed under the License is distributed on an \"AS IS\" BASIS,<br> WITHOUT WARRANTIES OR CONDITIONS OF\n" +
                "ANY KIND, either express or implied.<br> See the License for the specific language governing permissions and<br>\n" +
                "limitations under the License.<br>\n" +
                "</body>\n" +
                "</html>";
    }
}

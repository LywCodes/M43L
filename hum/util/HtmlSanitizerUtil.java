package ita.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class HtmlSanitizerUtil {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowCommonBlockElements()
            .allowElements(
                    "html", "body", "div", "p", "br", "span", "h1", "h2", "h3", "h4", "h5", "h6",
                    "a", "img", "strong", "em", "b", "i", "u", "strike", "sub", "sup", "ol", "ul", "li"
            )
            .allowElements(
                    "table", "thead", "tbody", "tfoot", "tr", "td", "th","img"
            )
            .allowAttributes("id", "class", "style", "dir").globally()
            .allowAttributes("href", "target", "title").onElements("a")
            .allowAttributes("src", "alt", "title", "width", "height", "align").onElements("img")
            .allowAttributes(
                    "cellpadding", "cellspacing", "border", "align", "valign",
                    "bgcolor", "width", "height", "colspan", "rowspan"
            ).onElements("table", "tr", "td", "th")
            .allowUrlProtocols("data")
            .allowStyling()
            .toFactory();

    public static String sanitize(String html) {
        return POLICY.sanitize(html);
    }

}

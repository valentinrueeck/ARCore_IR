package com.example.vrueeck.arcore_ir;

public  class PaintingDescriptionTextRetriever {

    public static String retrieveDescriptionText(String imageName){
        switch (imageName) {
            case "Segelboote":
                return "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
            case "VanGogh":
                return "Sternennacht (niederländisch De sterrennacht) ist eines der bekanntesten Gemälde des niederländischen Künstlers Vincent van Gogh. Er malte das 73,7 × 92,1 cm große Bild im Juni 1889 im französischen Saint-Rémy-de-Provence im Stil des Post-Impressionismus bzw. frühen Expressionismus[1] mit Ölfarben auf Leinwand. Das Bild ist seit 1941 im Besitz des Museum of Modern Art in New York City und wird dort unter dem Titel The Starry Night gezeigt. ";
            case "Wald":
                return "Wald (Waldung) im alltagssprachlichen Sinn und im Sinn der meisten Fachsprachen ist ein Ausschnitt der Erdoberfläche, der mit Bäumen bedeckt ist und die eine gewisse, vom Deutungszusammenhang abhängige Mindestdeckung und Mindestgröße überschreitet. Die Definition von Wald ist notwendigerweise vage[1] und hängt vom Bedeutungszusammenhang (alltagssprachlich, geographisch, biologisch, juristisch, ökonomisch, kulturell …) ab.";
            default:
                return "";
        }
    }
}

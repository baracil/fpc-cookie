module fpc.cookie {
    requires static lombok;
    requires java.desktop;

    exports net.femtoparsec.cookie;

    opens net.femtoparsec.cookie to com.fasterxml.jackson.databind;
}

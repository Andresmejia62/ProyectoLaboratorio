module com.appregistros.proyectolaboratorio {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.postgresql.jdbc;

    opens com.appregistros.proyectolaboratorio to javafx.fxml;
    opens com.appregistros.proyectolaboratorio.model to org.hibernate.orm.core;
    exports com.appregistros.proyectolaboratorio;
    exports com.appregistros.proyectolaboratorio.model;
}
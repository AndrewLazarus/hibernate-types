package com.vladmihalcea.hibernate.type.json.generic;

import com.vladmihalcea.hibernate.type.json.JsonType;
import com.vladmihalcea.hibernate.util.AbstractOracleIntegrationTest;
import com.vladmihalcea.hibernate.util.transaction.JPATransactionFunction;
import org.hibernate.Session;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.junit.Test;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class GenericOracleJsonMapTest extends AbstractOracleIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Book.class
        };
    }

    @Test
    public void test() {

        doInJPA(new JPATransactionFunction<Void>() {

            @Override
            public Void apply(EntityManager entityManager) {
                entityManager.persist(
                    new Book()
                        .setIsbn("978-9730228236")
                        .addProperty("title", "High-Performance Java Persistence")
                        .addProperty("author", "Vlad Mihalcea")
                        .addProperty("publisher", "Amazon")
                        .addProperty("price", "$44.95")
                );

                return null;
            }
        });

        doInJPA(new JPATransactionFunction<Void>() {

            @Override
            public Void apply(EntityManager entityManager) {
                Book book = (Book) entityManager.unwrap(Session.class)
                    .bySimpleNaturalId(Book.class)
                    .load("978-9730228236");

                Map<String, String> bookProperties = book.getProperties();

                assertEquals(
                    "High-Performance Java Persistence",
                    bookProperties.get("title")
                );

                assertEquals(
                    "Vlad Mihalcea",
                    bookProperties.get("author")
                );

                return null;
            }
        });
    }

    @Entity(name = "Book")
    @Table(name = "book")
    @TypeDef(name = "json", typeClass = JsonType.class)
    public static class Book {

        @Id
        @GeneratedValue
        private Long id;

        @NaturalId
        @Column(length = 15)
        private String isbn;

        @Type(type = "json")
        @Column(columnDefinition = "VARCHAR2(1000) CONSTRAINT IS_VALID_JSON CHECK (properties IS JSON)")
        private Map<String, String> properties = new HashMap<String, String>();

        public String getIsbn() {
            return isbn;
        }

        public Book setIsbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public Book setProperties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        public Book addProperty(String key, String value) {
            properties.put(key, value);
            return this;
        }
    }
}
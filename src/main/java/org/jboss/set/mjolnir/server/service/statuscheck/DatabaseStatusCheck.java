package org.jboss.set.mjolnir.server.service.statuscheck;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

/**
 * Checks that database is available and required tables are present.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
public class DatabaseStatusCheck extends AbstractStatusCheck {

    private static final String TITLE = "Database";

    private static final String[] REQUIRED_TABLES = new String[] {
            "application_parameters",
            "users",
            "github_teams",
            "github_orgs"
    };

    @Inject
    private EntityManagerFactory entityManagerFactory;

    public DatabaseStatusCheck() {
        super(TITLE);
    }

    @Override
    protected StatusCheckResult doCheckStatus() {
        StatusCheckResult result = new StatusCheckResult();
        try {
            List<String> tables = getTables();

            for (String requiredTable: REQUIRED_TABLES) {
                if (!tables.contains(requiredTable) && !tables.contains(requiredTable.toUpperCase())) {
                    result.addProblem(String.format("Missing table '%s'", requiredTable));
                }
            }
        } catch (Exception e) {
            result.addProblem("Database connection problem", e);
        }
        return result;
    }


    private List<String> getTables() throws Exception {
        EntityManager em = entityManagerFactory.createEntityManager();
        ResultSet resultSet = em.unwrap(Session.class).doReturningWork(new ReturningWork<ResultSet>() {
            @Override
            public ResultSet execute(Connection connection) throws SQLException {
                return connection.getMetaData().getTables("", null, null, new String[]{"TABLE"});
            }
        });
        List<String> tables = new ArrayList<>();
        while (resultSet.next()) {
            tables.add(resultSet.getString("TABLE_NAME"));
        }
        return tables;
    }
}
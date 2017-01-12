package util;

import java.sql.*;
import java.util.*;
import javax.ejb.Singleton;
import javax.naming.*;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

@Singleton
public class EventLogger {
    private static PreparedStatement preparedStatement;

    public EventLogger() {
        Connection connection = getConnection();
        try {
            String logEvent = "INSERT INTO logs (user, action, url, datetime, parameters) VALUES (?,?,?, now(), ?);";
            preparedStatement = connection.prepareStatement(logEvent);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void logEvent(HttpServletRequest request) {
        logEvent(request, null);
    }

    public static void logEvent(HttpServletRequest request, String action) {
        try {
            preparedStatement.setString(1, request.getRemoteUser());
            preparedStatement.setString(3, request.getRequestURI());

            if (action != null) {
                preparedStatement.setString(2, action);
                if(action.contains("login")) {
                    preparedStatement.setString(3, request.getHeader("Referer"));
                }
            } else {
                preparedStatement.setString(2, request.getMethod());
            }


            if(request.getMethod().equals("POST") || request.getMethod().equals("GET")) {
                preparedStatement.setString(4, convertParametersToString(request.getParameterMap()));
            }
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String convertParametersToString(Map<String, String[]> parametersMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
            sb.append(entry.getKey()).append("[").append(entry.getValue()[0]).append("]").append("_");
        }
        return sb.toString();
    }

    private Connection getConnection() {
        Hashtable ht = new Hashtable();
        ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        ht.put(Context.PROVIDER_URL, "t3://localhost:7001");

        Connection connection = null;
        try {
            InitialContext context = new InitialContext(ht);
            DataSource dataSource = (DataSource) context.lookup("jdbc/mysql_ds");
            connection = dataSource.getConnection();
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}

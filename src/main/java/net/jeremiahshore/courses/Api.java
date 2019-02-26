package net.jeremiahshore.courses;

import com.google.gson.Gson;
import net.jeremiahshore.courses.dao.CourseDao;
import net.jeremiahshore.courses.dao.Sql2oCourseDao;
import net.jeremiahshore.courses.model.Course;
import org.sql2o.Sql2o;

import static spark.Spark.*;

public class Api {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_DATASOURCE = "jdbc:h2:~/reviews.db";
    private static final String INIT_SCRIPT_PATH = ";INIT=RUNSCRIPT from 'classpath:db/init.sql'";

    private static Gson gson = new Gson();
    private static String datasource = DEFAULT_DATASOURCE;

    public static void main(String[] args) {
        processArgs(args);
        Sql2o sql2o = getConfiguredSql2o(datasource);
        CourseDao courseDao = new Sql2oCourseDao(sql2o);
        defineRoutes(courseDao, gson);
    }

    private static void processArgs(String[] args) {
        if(args.length > 0) {
            if(args.length != 2) {
                System.out.println("java Api <port> <datasource>");
                System.exit(0);
            }
            port(Integer.parseInt(args[0]));
            datasource = args[1];
        }
    }

    static Sql2o getConfiguredSql2o(String datasource) {
        return new Sql2o(datasource + INIT_SCRIPT_PATH, "", "");
    }

    private static void defineRoutes(CourseDao courseDao, Gson gson) {
        post("/courses", JSON_CONTENT_TYPE, (request, response) -> {
            Course course = gson.fromJson(request.body(), Course.class);
            courseDao.add(course);
            response.status(201);
            return course;
        }, gson::toJson); //response transform

        get("/courses", JSON_CONTENT_TYPE,
                (request, response) -> courseDao.findAll(), gson::toJson);

        get("/courses/:id", JSON_CONTENT_TYPE, (request, response) -> {
            int id = Integer.parseInt(request.params("id"));
            //todo: what if this is not found?
            return courseDao.findById(id);
        }, gson::toJson);

        after((request, response) -> response.type(JSON_CONTENT_TYPE));
    }


}
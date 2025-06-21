package com.mlvisio.servlets;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

@WebServlet("/api/stats/dashboard")
public class DashboardStatsServlet extends HttpServlet {

    private Firestore db;

    @Override
    public void init() throws ServletException {
        try {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
            if (serviceAccount == null) throw new RuntimeException("Missing serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();
        } catch (Exception e) {
            throw new ServletException("Firebase init failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> data = new HashMap<>();
        try {
            
            ApiFuture<QuerySnapshot> userQuery = db.collection("users").get();
            List<QueryDocumentSnapshot> users = userQuery.get().getDocuments();
            data.put("totalStudents", users.size());

            
            String today = LocalDate.now().toString();
            ApiFuture<QuerySnapshot> attendanceQuery = db.collection("attendance")
                    .whereEqualTo("date", today)
                    .get();
            List<QueryDocumentSnapshot> attendanceToday = attendanceQuery.get().getDocuments();
            data.put("presentToday", attendanceToday.size());

            
            double rate = users.isEmpty() ? 0 : ((double) attendanceToday.size() / users.size()) * 100;
            data.put("attendanceRate", (int) Math.round(rate));

            
            ApiFuture<QuerySnapshot> coursesQuery = db.collection("courses").get();
            List<QueryDocumentSnapshot> courses = coursesQuery.get().getDocuments();
            data.put("totalCourses", courses.size());

            
            Map<String, Integer> deptTotals = new HashMap<>();
            Map<String, Integer> deptPresent = new HashMap<>();
            for (QueryDocumentSnapshot user : users) {
                String dept = user.getString("department");
                if (dept != null) {
                    deptTotals.put(dept, deptTotals.getOrDefault(dept, 0) + 1);
                }
            }
            for (QueryDocumentSnapshot att : attendanceToday) {
                String dept = att.getString("department"); 
                if (dept != null) {
                    deptPresent.put(dept, deptPresent.getOrDefault(dept, 0) + 1);
                }
            }
            List<Map<String, Object>> departmentAttendance = new ArrayList<>();
            for (String dept : deptTotals.keySet()) {
                int present = deptPresent.getOrDefault(dept, 0);
                int total = deptTotals.get(dept);
                int deptRate = total == 0 ? 0 : (int) Math.round(((double) present / total) * 100);
                departmentAttendance.add(Map.of("department", dept, "rate", deptRate));
            }
            data.put("departmentAttendance", departmentAttendance);

            
            int fullTime = 0, partTime = 0;
            for (QueryDocumentSnapshot user : users) {
                String mode = user.getString("studyMode");
                if ("full-time".equalsIgnoreCase(mode)) fullTime++;
                else partTime++;
            }
            Map<String, Integer> studyModeCounts = Map.of("fullTime", fullTime, "partTime", partTime);
            data.put("studyModeCounts", studyModeCounts);

            
            resp.setContentType("application/json");
            new ObjectMapper().writeValue(resp.getWriter(), Map.of("success", true, "data", data));

        } catch (InterruptedException | ExecutionException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            new ObjectMapper().writeValue(resp.getWriter(), Map.of("success", false, "message", e.getMessage()));
        }
    }
}

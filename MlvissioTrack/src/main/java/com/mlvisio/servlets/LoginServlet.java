package com.mlvisio.servlets;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.mlvisio.util.FirebaseInitializer;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        FirebaseInitializer.initialize(); // ✅ Initialize Firebase
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonResponse = new JSONObject();

        try {
            // 📥 Parse JSON request
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonRequest = new JSONObject(sb.toString());
            String email = jsonRequest.optString("email");
            String password = jsonRequest.optString("password");

            // ❗ Basic validation
            if (email.isEmpty() || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Email and password are required.");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            // 🔍 Firestore query
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection("users")
                    .whereEqualTo("email", email)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Invalid email or password.");
            } else {
                DocumentSnapshot userDoc = documents.get(0);
                String storedHash = userDoc.getString("password");

                if (storedHash != null && BCrypt.checkpw(password, storedHash)) {
                    // ✅ Auth success
                    JSONObject userData = new JSONObject();
                    userData.put("id", userDoc.getId());
                    userData.put("email", userDoc.getString("email"));
                    userData.put("name", userDoc.getString("fullName")); // 🔄 Send as "name"
                    userData.put("registrationNumber", userDoc.getString("registrationNumber"));
                    userData.put("vertexLabel", userDoc.getString("vertexLabel"));
                    userData.put("department", userDoc.getString("department")); // Optional
                    userData.put("role", userDoc.getString("role") != null ? userDoc.getString("role") : "user");

                    jsonResponse.put("success", true);
                    jsonResponse.put("message", "Login successful.");
                    jsonResponse.put("data", new JSONObject()
                            .put("user", userData)
                            .put("token", "mock-token")); // 🔐 Replace with real JWT later
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Invalid email or password.");
                }
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Server error: " + e.getMessage());
            e.printStackTrace();
        }

        response.getWriter().write(jsonResponse.toString());
    }
}

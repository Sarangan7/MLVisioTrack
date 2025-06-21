package util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SeedUsers {

    public static void main(String[] args) {
        try {
            InputStream serviceAccount = SeedUsers.class.getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount == null) {
                System.err.println("❌ Firebase serviceAccountKey.json not found.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);

            Firestore db = FirestoreClient.getFirestore();

            String[][] users = {
                    {"T.Sarankan", "JAF/IT/2022/P/050", "sarangan@mlvisio.com", "sarangan", "user"},
                    {"Admin User", "ADMIN-001", "admin@mlvisio.com", "admin", "admin"},
                    {"P.Nallamuthan", "JAF/IT/2022/P/026", "nallamuthan@mlvisio.com", "nallamuthan", "user"},
                    {"K.Varna", "JAF/IT/2022/P/027", "varna@mlvisio.com", "varna", "user"},
                    {"K.Jeyanthan", "JAF/IT/2022/P/005", "jeyan@mlvisio.com", "jeyan", "user"},
                    {"B.Jeroshan", "JAF/IT/2022/P/017", "jerosan@mlvisio.com", "jerosan", "user"},
                    {"S.Thenujan", "JAF/IT/2022/P/044", "thenujan@mlvisio.com", "thenujan", "user"},
                    {"T.Yathuppriyan", "JAF/IT/2022/P/031", "priyan@mlvisio.com", "priyan", "user"},
                    {"M.Megaruban", "JAF/IT/2022/P/056", "rooban@mlvisio.com", "rooban", "user"},
                    {"T.Thilagavathy", "JAF/IT/2022/P/063", "thilagavathy@mlvisio.com", "thilagavathy", "user"},
                    {"S.Gajan", "JAF/IT/2022/P/064", "gajan@mlvisio.com", "gajan", "user"}
            };

            for (String[] u : users) {
                String fullName = u[0];
                String registrationNumber = u[1];
                String email = u[2];
                String vertexLabel = u[3];
                String role = u[4];

                String plainPassword = registrationNumber;
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

                Map<String, Object> user = new HashMap<>();
                user.put("fullName", fullName);
                user.put("registrationNumber", registrationNumber);
                user.put("email", email);
                user.put("vertexLabel", vertexLabel);
                user.put("role", role);
                user.put("password", hashedPassword);
                user.put("profilePicture", null);
                user.put("department", "HNDIT");

                if (role.equals("admin")) {
                    user.put("adminLevel", "Regular Admin");
                } else {
                    user.put("studyMode", "part-time");
                }

                db.collection("users").document(email).set(user).get();
                System.out.println("✅ Seeded: " + email + " | Password: " + plainPassword);
            }

            System.out.println("\n🎉 All users seeded successfully without joinDate, with studyMode for users!");

        } catch (Exception e) {
            System.err.println("❌ Error during seeding:");
            e.printStackTrace();
        }
    }
}

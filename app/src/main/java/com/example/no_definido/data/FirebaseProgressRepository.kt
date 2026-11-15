package com.example.no_definido.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class UserProgress(
    val userId: String = "jugador_invitado",
    val applesCollectedTotal: Int = 0,
    val objectGamesCompleted: Int = 0,
    val totalScore: Int = 0,
    val totalStars: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

class FirebaseProgressRepository(
    private val localDataSource: LocalProgressDataSource = LocalProgressDataSource.getInstance(),
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        }
    }
) : ProgressRepository {

    private val collectionName = "usuarios_progreso"

    /**
     * Guarda primero en caché local (`LocalProgressDataSource`) para garantizar persistencia
     * inmediata sin conexión y en pruebas JVM, e intenta sincronizar con Firestore si está disponible.
     */
    override suspend fun saveProgress(progress: UserProgress): Boolean {
        val savedLocally = localDataSource.saveProgress(progress)
        return try {
            val db = firestoreProvider() ?: return savedLocally
            withContext(Dispatchers.IO) {
                db.collection(collectionName)
                    .document(progress.userId)
                    .set(progress)
                    .await()
            }
            true
        } catch (_: Throwable) {
            savedLocally
        }
    }

    /**
     * Obtiene el progreso desde la caché local primero; si no existe localmente,
     * intenta descargarlo desde Firestore y sincronizarlo en local.
     */
    override suspend fun getProgress(userId: String): UserProgress? {
        val local = localDataSource.getProgress(userId)
        if (local != null) {
            return local
        }
        return try {
            val db = firestoreProvider() ?: return null
            val remote = withContext(Dispatchers.IO) {
                val snapshot = db.collection(collectionName)
                    .document(userId)
                    .get()
                    .await()
                if (snapshot.exists()) {
                    snapshot.toObject(UserProgress::class.java)
                } else {
                    null
                }
            }
            if (remote != null) {
                localDataSource.saveProgress(remote)
            }
            remote
        } catch (_: Throwable) {
            null
        }
    }
}

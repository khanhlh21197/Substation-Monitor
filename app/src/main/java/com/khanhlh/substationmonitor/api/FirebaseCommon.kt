package com.khanhlh.substationmonitor.api

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.extensions.set
import durdinapps.rxfirebase2.RxFirestore
import io.reactivex.Observable


object FirebaseCommon {
    @SuppressLint("CheckResult")
    fun firestore(
        store: FirebaseFirestore,
        collection: String,
        document: String
    ): MutableLiveData<DocumentSnapshot> {
        val documentSnapshot = MutableLiveData<DocumentSnapshot>()
        val docRef = store.collection(collection).document(document)
        RxFirestore.observeDocumentRef(docRef).subscribe { documentSnapshot.set(it) }
        return documentSnapshot
    }

    fun getListDocument(collection: String): Observable<List<String>> {
        return Observable.create { emitter ->
            db.collection(collection).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val docs = mutableListOf<String>()
                    for (doc: QueryDocumentSnapshot in it.result!!) {
                        docs.add(doc.id)
                    }
                    emitter.onNext(docs)
                } else {
                    logD(it.exception.toString())
                }
            }
        }
    }

    fun <K, V> push(
        collection: String,
        document: String,
        mapValue: HashMap<K, V>
    ): Observable<DocumentReference> =
        Observable.create { emitter ->
            db.collection(collection).document(document)
                .set(mapValue)
                .addOnSuccessListener {
                    emitter.onComplete()
                    logD("onSuccess")
                }
                .addOnFailureListener {
                    emitter.onError(it)
                    logD("onError")
                }
        }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
}
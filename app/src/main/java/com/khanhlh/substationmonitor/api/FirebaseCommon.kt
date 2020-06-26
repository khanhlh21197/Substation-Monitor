package com.khanhlh.substationmonitor.api

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.utils.DEVICES
import durdinapps.rxfirebase2.RxFirestore
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single


object FirebaseCommon {
    @SuppressLint("CheckResult")
    fun firestore(
        collection: String,
        document: String
    ): Flowable<DocumentSnapshot> {
        val docRef = db.collection(collection).document(document)
        return RxFirestore.observeDocumentRef(docRef)
    }

    fun getListDocument(collection: String): Single<QuerySnapshot> {
        return Single.create { emitter ->
            db.collection(collection).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    emitter.onSuccess(it.result!!)
                } else {
                    logD(it.exception.toString())
                    emitter.onError(it.exception!!)
                }
            }
        }
    }

//    fun observerAllDevices(): Observable<Map<String, Any>> =
//        Observable.create { emitter ->
//            run {
//                db.collection(DEVICES).addSnapshotListener { querySnapshot, e ->
//                    if (e != null) {
//                        logD(e.toString())
//                        emitter.onError(e)
//                        return@addSnapshotListener
//                    } else {
//                        querySnapshot!!.forEach {
//                            logD(it.toString())
//                            emitter.onNext(it.data)
//                        }
//                    }
//                }
//            }
//        }

    fun observerAllDevices(): Observable<QueryDocumentSnapshot> =
        Observable.create { emitter ->
            run {
                db.collection(DEVICES).addSnapshotListener { querySnapshot, e ->
                    if (e != null) {
                        logD(e.toString())
                        emitter.onError(e)
                        return@addSnapshotListener
                    } else {
                        querySnapshot!!.forEach {
                            logD(it.toString() + System.currentTimeMillis())
                            emitter.onNext(it)
                        }
                    }
                }
            }
        }

    fun observerAllDevice() = RxFirestore.getCollection(db.collection(DEVICES))

    fun observerDevice(doc: String): Observable<DocumentSnapshot> =
        Observable.create { emitter ->
            apply {
                db.collection(DEVICES).document(doc)
                    .addSnapshotListener { documentSnapshot, e ->
                        if (e != null) {
                            emitter.onError(e)
                            return@addSnapshotListener
                        } else {
                            emitter.onNext(documentSnapshot!!)
                            logD(documentSnapshot.toString())
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

    public interface Callback<T> {
        fun onSuccess(t: T)
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
}
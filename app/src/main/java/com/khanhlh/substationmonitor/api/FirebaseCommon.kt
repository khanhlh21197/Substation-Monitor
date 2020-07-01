package com.khanhlh.substationmonitor.api

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.ui.main.fragments.home.UpdateDeviceType
import com.khanhlh.substationmonitor.utils.DEVICES
import com.khanhlh.substationmonitor.utils.USER_COLLECTION
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

    fun getProfile(): Observable<DocumentSnapshot> = Observable.create { emitter ->
        db.collection(USER_COLLECTION).document(currentUser!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                emitter.onNext(it.result!!)
            } else {
                logD(it.exception.toString())
            }
        }
    }

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

    fun observerDevice(id: String): Observable<DocumentSnapshot> =
        Observable.create { emitter ->
            apply {
                db.collection(DEVICES).document(id)
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

    fun update(
        collection: String,
        document: String,
        key: String,
        value: String
    ): Observable<String> =
        Observable.create { emitter ->
            db.collection(collection).document(document)
                .update(key, value)
                .addOnSuccessListener {
                    emitter.onNext("Success")
                }
                .addOnFailureListener {
                    emitter.onError(it)
                    logD("onError")
                }
        }

    fun getDevicesOfUser(): Observable<String> {
        return Observable.create { emitter ->
            db.collection(USER_COLLECTION).document(currentUser!!.uid).get()
                .addOnSuccessListener {
                    emitter.onNext(it[DEVICES].toString())
                    logD(it[DEVICES] as String?)
                }
        }
    }

    fun updateDevice(id: String, add: UpdateDeviceType): Observable<String> {
        return Observable.create { emitter ->
            getDevicesOfUser().map {
                when (add) {
                    UpdateDeviceType.ADD -> "$it,$id"
                    UpdateDeviceType.REMOVE -> it.replace(id, "")
                }
            }.subscribe({ emitter.onNext("Thành công") }, { emitter.onError(it) })
        }
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
}
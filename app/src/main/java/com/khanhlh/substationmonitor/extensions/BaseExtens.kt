package com.khanhlh.substationmonitor.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.khanhlh.substationmonitor.BuildConfig
import com.khanhlh.substationmonitor.exception.EmptyException
import com.khanhlh.substationmonitor.helper.annotation.ToastType
import com.khanhlh.substationmonitor.utils.KEY_SERIALIZABLE
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.MainThreadDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.internal.and
import java.io.Serializable
import java.net.ConnectException
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * 页面描述：一些扩展
 *
 * Created by ditclear on 2017/9/29.
 */

fun Activity.getCompactColor(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

fun Activity.toast(
    msg: CharSequence,
    duration: Int = Toast.LENGTH_SHORT,
    @ToastType type: Int = ToastType.NORMAL
) {
    when (type) {
        ToastType.WARNING,
        ToastType.ERROR,
        ToastType.NORMAL,
        ToastType.SUCCESS -> runOnUiThread(Runnable { Toast.makeText(this, msg, duration).show() })
    }
}

@SuppressLint("ResourceType")
fun Activity.toast(
    @LayoutRes
    msg: Int,
    duration: Int = Toast.LENGTH_SHORT,
    @ToastType type: Int = ToastType.NORMAL
) {
    when (type) {
        ToastType.WARNING,
        ToastType.ERROR,
        ToastType.NORMAL,
        ToastType.SUCCESS -> runOnUiThread(Runnable {
            Toast.makeText(
                this,
                this.getString(msg),
                duration
            ).show()
        })
    }
}

fun Activity.dispatchFailure(error: Throwable?) {
    error?.let {
        if (BuildConfig.DEBUG) {
            it.printStackTrace()
        }
        if (it is EmptyException) {

        } else if (error is SocketTimeoutException) {
            it.message?.let { toast("网络连接超时", ToastType.ERROR) }

        } else if (it is UnknownHostException || it is ConnectException) {
            //网络未连接
            it.message?.let { toast("网络未连接", ToastType.ERROR) }

        } else {
            it.message?.let { toast(it, ToastType.ERROR) }
        }
    }
}

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun <T : Any> androidx.fragment.app.FragmentActivity.argument(key: String) =
    lazy { intent.extras!![key] as? T ?: error("Intent Argument $key is missing") }

fun AppCompatActivity.switchFragment(
    current: Fragment?,
    targetFg: Fragment,
    tag: String? = null
) {
    val ft = supportFragmentManager.beginTransaction()
    current?.run { ft.hide(this) }
    if (!targetFg.isAdded) {
        ft.add(com.khanhlh.substationmonitor.R.id.nav_host_container, targetFg, tag)
    }
    ft.show(targetFg)
    ft.commitAllowingStateLoss();
}

fun Fragment.navigate(@IdRes idRes: Int, args: Bundle? = null) =
    NavHostFragment.findNavController(this).navigate(idRes, args)

fun AppCompatActivity.replaceFragmentSafely(
    fragment: Fragment,
    tag: String,
    allowStateLoss: Boolean = false,
    @AnimRes enterAnimation: Int = 0,
    @AnimRes exitAnimation: Int = 0,
    @AnimRes popEnterAnimation: Int = 0,
    @AnimRes popExitAnimation: Int = 0
) {
    val ft = supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        .replace(com.khanhlh.substationmonitor.R.id.nav_host_container, fragment, tag)
    if (!supportFragmentManager.isStateSaved) {
        ft.commit()
    } else if (allowStateLoss) {
        ft.commitAllowingStateLoss()
    }
}

fun AppCompatActivity.addFragmentSafely(
    fragment: Fragment,
    tag: String,
    allowStateLoss: Boolean = false,
    @AnimRes enterAnimation: Int = 0,
    @AnimRes exitAnimation: Int = 0,
    @AnimRes popEnterAnimation: Int = 0,
    @AnimRes popExitAnimation: Int = 0
) {
    val ft = supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        .add(com.khanhlh.substationmonitor.R.id.nav_host_container, fragment, tag)
    if (!supportFragmentManager.isStateSaved) {
        ft.commit()
    } else if (allowStateLoss) {
        ft.commitAllowingStateLoss()
    }
}

fun Activity.dpToPx(@DimenRes resID: Int): Int = this.resources.getDimensionPixelOffset(resID)

fun <T> toJson(t: T): String? {
    val gson = Gson()
    return gson.toJson(t)
}

inline fun <reified T> fromJson(input: String): T {
    val gson = Gson()
    return gson.fromJson(input, T::class.java)
}

fun Activity.navigateToActivity(c: Class<*>, serializable: Serializable? = null) {
    val intent = Intent()
    serializable?.let {
        val bundle = Bundle()
        bundle.putSerializable(KEY_SERIALIZABLE, it)
        intent.putExtras(bundle)
    }
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()

    intent.setClass(this, c)
    startActivity(intent, options)
}

fun Any.logD(msg: String?) {
    if (BuildConfig.DEBUG) {
        Log.d(javaClass.simpleName, msg)
    }
}

fun <T> Flowable<T>.async(withDelay: Long = 0): Flowable<T> =
    this.subscribeOn(Schedulers.io()).delay(withDelay, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.async(withDelay: Long = 0): Single<T> =
    this.subscribeOn(Schedulers.io()).delay(withDelay, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())

//fun <R : BaseResponse> Single<R>.getOriginData(): Single<R> {
//    return this.compose { upstream ->
//        upstream.flatMap { t: R ->
//            with(t) {
//                if (t.errorCode.equals("0")) {
//                    return@flatMap Single.just(t)
//                } else {
//                    return@flatMap Single.error<R>(Throwable(message))
//                }
//            }
//        }
//    }
//}

fun <T> Single<T>.bindLifeCycle(owner: LifecycleOwner): SingleSubscribeProxy<T> =
    this.`as`(
        AutoDispose.autoDisposable(
            AndroidLifecycleScopeProvider.from(
                owner,
                Lifecycle.Event.ON_DESTROY
            )
        )
    )

fun <T> Flowable<T>.bindLifeCycle(owner: LifecycleOwner): FlowableSubscribeProxy<T> =
    this.`as`(
        AutoDispose.autoDisposable(
            AndroidLifecycleScopeProvider.from(
                owner,
                Lifecycle.Event.ON_DESTROY
            )
        )
    )


//////////////////////////LiveData///////////////////////////////////

fun <T> MutableLiveData<T>.set(t: T?) = this.postValue(t)
fun <T> MutableLiveData<T>.get() = this.value

fun <T> MutableLiveData<T>.get(t: T): T = get() ?: t

fun <T> MutableLiveData<T>.init(t: T) = MutableLiveData<T>().apply {
    postValue(t)
}

fun <T> LiveData<T>.toFlowable(): Flowable<T> = Flowable.create({ emitter ->
    val observer = Observer<T> { data ->
        data?.let { emitter.onNext(it) }
    }
    observeForever(observer)

    emitter.setCancellable {
        object : MainThreadDisposable() {
            override fun onDispose() = removeObserver(observer)
        }
    }
}, BackpressureStrategy.LATEST)

//////////////////////////DataBinding///////////////////////////////////
fun <T> ObservableField<T>.toFlowable(): Flowable<T> = Flowable.create({ emitter ->
    val observer = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            get()?.let { emitter.onNext(it) }
        }
    }
    addOnPropertyChangedCallback(observer)

    emitter.setCancellable {
        object : MainThreadDisposable() {
            override fun onDispose() = removeOnPropertyChangedCallback(observer)
        }
    }
}, BackpressureStrategy.LATEST)

fun ObservableBoolean.toFlowable(): Flowable<Boolean> = Flowable.create({ emitter ->
    val observer = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            emitter.onNext(get())
        }
    }
    addOnPropertyChangedCallback(observer)

    emitter.setCancellable {
        object : MainThreadDisposable() {
            override fun onDispose() = removeOnPropertyChangedCallback(observer)
        }
    }
}, BackpressureStrategy.LATEST)

fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

fun getMacAddr(): String? {
    try {
        val all: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (nif in all) {
            if (nif.name != "wlan0") continue
            val macBytes: ByteArray = nif.hardwareAddress ?: return ""
            val res1 = StringBuilder()
            for (b in macBytes) {
                res1.append(Integer.toHexString(b and 0xFF) + ":")
            }
            if (res1.isNotEmpty()) {
                res1.deleteCharAt(res1.length - 1)
            }
            return res1.toString().replace(":", "")
        }
    } catch (ex: Exception) {
    }
    return "02:00:00:00:00:00"
}

@SuppressLint("LogNotTimber")
fun createDialog(
    act: Activity, message: String?,
    title: String?, leftButtonText: String?, rightButtonText: String?,
    leftClick: View.OnClickListener?, rightClick: View.OnClickListener?
) {
    return try {
        MaterialDialog.Builder(act)
            .title(title!!)
            .content(message!!)
            .positiveText(rightButtonText!!)
            .negativeText(leftButtonText!!)
            .onPositive { dialog, which ->
                rightClick?.onClick(act.currentFocus)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            .onNegative { dialog, which ->
                leftClick?.onClick(act.currentFocus)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }.build()
            .show()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

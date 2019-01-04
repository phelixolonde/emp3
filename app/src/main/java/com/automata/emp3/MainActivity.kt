package com.automata.emp3

import android.Manifest
import android.annotation.TargetApi
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.facebook.ads.*
import com.victor.loading.rotate.RotateLoading
import com.wang.avi.AVLoadingIndicatorView
import hotchemi.android.rate.AppRate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.text.DecimalFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    var songList: MutableList<String> = ArrayList()
    lateinit var list: ListView
    lateinit var txtSearch: AutoCompleteTextView
    lateinit var btnSearch: ImageView
    internal var data: ArrayList<Song> = ArrayList()
    lateinit var mSelectedTrackTitle: TextView
    lateinit var mMediaPlayer: MediaPlayer
    lateinit var mPlayerControl: ImageView
    lateinit var txtTime: TextView
    lateinit var txtDuration: TextView
    lateinit var seekBar: SeekBar
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()
    lateinit var btnDownload: ImageView
    private val WRITEREQUESTCODE = 300
    lateinit var toolbar: Toolbar

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "com.automata.emp3"
    private val description = "emp3"
    lateinit var loading: RotateLoading
    lateinit var avi: AVLoadingIndicatorView
    lateinit var downloadManager: DownloadManager
    var refid: Long = 0
    var listDownload: ArrayList<Long> = ArrayList()

    var nativeBannerAd: NativeBannerAd? = null
    private var nativeBannerAdContainer: RelativeLayout? = null
    private var adView: LinearLayout? = null
    private val TAG = "FACEBOOK_ADS"
    private var interstitialAd: InterstitialAd? = null


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        list = findViewById(R.id.listSongs)
        txtSearch = findViewById(R.id.txtSearch)
        btnSearch = findViewById(R.id.btnSearch)
        mSelectedTrackTitle = findViewById(R.id.selected_track_title)
        mPlayerControl = findViewById(R.id.player_control)
        btnDownload = findViewById(R.id.imgDownload)
        toolbar = findViewById(R.id.toolbar)
        loading = findViewById(R.id.rotateloading)
        avi = findViewById(R.id.avi)


        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )



        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                getSuggestion(s.toString().toLowerCase())

            }

            override fun afterTextChanged(s: Editable) {

            }
        })



        getSongs()

        btnSearch.setOnClickListener {

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(txtSearch.windowToken, 0)
            if (txtSearch.text.toString() != "") {


                val searchtext = txtSearch.text.toString().replace(" ", "+")

                getSearchSongs("https://db.mp3.direct/api/search_api.cgi?jsoncallback=jQuery17208209149017895265_1540918003775&qry=" + searchtext + "&format=json&mh=20&where=mdt&rel=new&r=&y=07350108160d5c53465d5c465d5d45595d445c534c5451465b51&_=1540918103693")

            }
        }


        mMediaPlayer = MediaPlayer()
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer.setOnPreparedListener {
            loading.stop()
            togglePlayPause()
        }
        mPlayerControl.setOnClickListener { togglePlayPause() }
        mMediaPlayer.setOnCompletionListener { mPlayerControl.setImageResource(R.drawable.ic_play) }

        txtTime = findViewById(R.id.time)
        txtDuration = findViewById(R.id.duration)

        seekBar = findViewById(R.id.seekBar)

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager


        interstitialAd = InterstitialAd(this, "265016387515108_265017794181634")
        interstitialAd?.setAdListener(object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.")
            }

            override fun onInterstitialDismissed(ad: Ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.")
            }

            override fun onError(ad: Ad, adError: AdError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.errorMessage)
            }

            override fun onAdLoaded(ad: Ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")
                // Show the ad
                interstitialAd?.show()
            }

            override fun onAdClicked(ad: Ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!")
            }
        })

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        interstitialAd?.loadAd()

        nativeBannerAd = NativeBannerAd(this, "265016387515108_265016957515051")
        nativeBannerAd!!.setAdListener(object : NativeAdListener {
            override fun onMediaDownloaded(ad: Ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.")
            }

            override fun onError(ad: Ad, adError: AdError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.errorMessage)

            }

            override fun onAdLoaded(ad: Ad) {

                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!")
                if (nativeBannerAd == null || nativeBannerAd !== ad) {
                    return
                }

                // Inflate Native Banner Ad into Container
                inflateAd(nativeBannerAd!!)
            }

            override fun onAdClicked(ad: Ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!")
            }
        })
        // load the ad
        nativeBannerAd?.loadAd()

    }

    private fun inflateAd(nativeBannerAd: NativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView()

        // Add the Ad view into the ad container.
        nativeBannerAdContainer = findViewById(R.id.native_banner_ad_container)
        val inflater = LayoutInflater.from(this@MainActivity)
        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        adView = inflater.inflate(R.layout.native_banner_ad_unit, nativeBannerAdContainer, false) as LinearLayout
        nativeBannerAdContainer?.addView(adView)


        // Add the AdChoices icon
        val adChoicesContainer = adView?.findViewById<RelativeLayout>(R.id.ad_choices_container)
        val adChoicesView = AdChoicesView(this@MainActivity, nativeBannerAd, true)
        adChoicesContainer?.addView(adChoicesView, 0)

        // Create native UI using the ad metadata.
        val nativeAdTitle = adView!!.findViewById<TextView>(R.id.native_ad_title)
        val nativeAdSocialContext = adView?.findViewById<TextView>(R.id.native_ad_social_context)
        val sponsoredLabel = adView?.findViewById<TextView>(R.id.native_ad_sponsored_label)
        val nativeAdIconView = adView?.findViewById<AdIconView>(R.id.native_icon_view)
        val nativeAdCallToAction = adView!!.findViewById<Button>(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdCallToAction?.text = nativeBannerAd.adCallToAction
        nativeAdCallToAction?.visibility = if (nativeBannerAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdTitle?.text = nativeBannerAd.advertiserName
        nativeAdSocialContext?.text = nativeBannerAd.adSocialContext
        sponsoredLabel?.text = nativeBannerAd.sponsoredTranslation

        // Register the Title and CTA button to listen for clicks.
        val clickableViews = ArrayList<View>()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)
        nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == android.R.id.home) {
            finish()
        }
        if (id == R.id.about) {
            val alert = AlertDialog.Builder(this)
            alert.setTitle("emp3")
            try {
                alert.setMessage(
                    "Version " + application.packageManager.getPackageInfo(packageName, 0).versionCode +
                            "\n" + "Automata Software. \n" + "\n" +
                            "All rights reserved \n"
                )
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            alert.show()
        } /*else if (id == R.id.feedback) {
            startActivity(Intent(this@MainActivity, Feedback::class.java))
        } else if (id == R.id.privacy) {
            val `in` = Intent(this@MainActivity, PrivacyPolicy::class.java)
            startActivity(`in`)
        }*/ else if (id == R.id.updaete) {
            val uri = Uri.parse("market://details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@MainActivity, "Unable to find play store", Toast.LENGTH_SHORT).show()
            }

        } else if (id == R.id.rate) {

            val uri = Uri.parse("market://details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@MainActivity, "Unable to find play store", Toast.LENGTH_SHORT).show()

            }
        } else if (id == android.R.id.home) {
            finish()
        } else if (id == R.id.menu_share) {
            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody =
                "Download and stream mp3 music with emp3 app.Download from https://play.google.com/store/apps/details?id=com.automata.emp3"
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "emp3 App")
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
            startActivity(sharingIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun togglePlayPause() {
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.pause()
            mPlayerControl.setImageResource(R.drawable.ic_play)
        } else {
            mMediaPlayer.start()
            mPlayerControl.setImageResource(R.drawable.ic_pause)

        }
        initializeSeekBar()


    }

    private fun initializeSeekBar() {
        seekBar.max = mMediaPlayer.seconds

        runnable = Runnable {
            seekBar.progress = mMediaPlayer.currentSeconds

            txtTime.text =
                    String.format("%02d:%02d", mMediaPlayer.currentSeconds / 60, mMediaPlayer.currentSeconds % 60)
            txtDuration.text = String.format("%02d:%02d", mMediaPlayer.seconds / 60, mMediaPlayer.seconds % 60)
            txtDuration.visibility = View.VISIBLE

            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                if (b) {
                    /*mMediaPlayer.seekTo(progress * 1000)
                    Toast.makeText(this@MainActivity, "p$progress", Toast.LENGTH_LONG).show()
                    seekBar.progress = progress*/
                    /* loading.start()
                     mMediaPlayer.seekTo(progress*1000)
                     if (mMediaPlayer.isPlaying){
                         loading.stop()
                     }*/

                }
                // seekBar.progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {


            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {


            }
        })
    }


    // Extension property to get media player duration in seconds
    private val MediaPlayer.seconds: Int
        get() {
            return this.duration / 1000
        }


    private val MediaPlayer.currentSeconds: Int
        get() {
            return this.currentPosition / 1000
        }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun getSearchSongs(url: String) {

        data.clear()

        val postRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->


                Log.d("S_RESPONSE", response)


                try {
                    val songarray = response.substring(response.indexOf("["), response.indexOf("]")) + "]"
                    // Log.d("SONGS_ARRAY", song_array)

                    val array = JSONArray(songarray)

                    for (i in 0 until array.length()) {
                        val article = array.get(i) as JSONObject
                        val song = Song()
                        song.title = article.getString("title")
                        song.artist = article.getString("artist")
                        song.url = article.getString("url")
                        song.link = "link"
                        song.image = article.getString("albumart")
                        data.add(song)
                    }
                    val adapter = CustomAdapter(data, this@MainActivity)

                    val label: TextView = findViewById(R.id.label)
                    label.visibility = View.GONE

                    list.adapter = adapter
                    adapter.notifyDataSetChanged()

                    list.setOnItemClickListener { parent, view, position, id ->
                        toolbar.visibility = View.VISIBLE

                        val song = data[position]

                        mSelectedTrackTitle.text = song.getArtist() + " - " + song.getTitle()


                        mMediaPlayer.stop()
                        mMediaPlayer.reset()



                        try {
                            playSong(song.getArtist(), song.getTitle(), song.getUrl())

                        } catch (e: Exception) {
                            Log.e("PLAY_SONG_ERROR", e.message, e)
                            Toast.makeText(
                                this@MainActivity,
                                resources.getString(R.string.check_internet),
                                Toast.LENGTH_LONG
                            ).show()

                        }

                    }

                } catch (e: Exception) {

                    Log.e("S_ERROR", e.message, e)
                }
            },
            Response.ErrorListener { error ->
                Log.e("S_ERROR", error.message, error)
                if (error is NoConnectionError) {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.check_internet), Toast.LENGTH_LONG)
                        .show()

                } else if (error is TimeoutError) {
                    Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.connection_timeout),
                        Toast.LENGTH_LONG
                    ).show()

                } else if (error is ServerError) {
                    Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.error_occured),
                        Toast.LENGTH_LONG
                    ).show()
                } else if (error is NetworkError) {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.check_internet), Toast.LENGTH_LONG)
                        .show()

                } else if (error is ParseError) {
                    Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.error_occured),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.error_occured),
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
        )

        emp3.getInstance().addToRequestQueue(postRequest, "SONG_REQUEST")

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun playSong(artist: String?, title: String?, url: String?) {

        loading.start()
        val mUrl =
            "https://db.mp3.direct/api/data_api_new.cgi?" +
                    "jsoncallback=jQuery17208412320667362989_1541770390111&id=" + url + "&r=new" +
                    "&format=json&_=1541770441784"


        val postRequest = StringRequest(
            Request.Method.GET, mUrl,
            Response.Listener { response ->


                Log.d("SEARCH_RESPONSE", response)


                try {
                    val song_ = response.substring(response.indexOf("(") + 1, response.length - 1)

                    val songObject = JSONObject(song_)
                    val jsonObject = songObject.getJSONObject("song")
                    val link = jsonObject.getString("url")

                    mMediaPlayer.setDataSource(link)
                    mMediaPlayer.prepareAsync()


                    try {
                        btnDownload.setOnClickListener {

                            if (CheckForSDCard.isSDCardPresent()) {


                                if (EasyPermissions.hasPermissions(
                                        this@MainActivity,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )
                                ) {


                                    Toast.makeText(
                                        this@MainActivity, resources.getString(R.string.downloading_check)
                                        ,
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val directory = File("/emp3")


                                    if (!directory.exists()) {
                                        directory.mkdir()

                                    }

                                    val file = File("$artist-$title.mp3")

                                    val downloadUri: Uri =
                                        Uri.parse(link)

                                    val request = DownloadManager.Request(downloadUri)
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                                    request.setAllowedOverRoaming(false)
                                    request.setTitle("$artist - $title")
                                    request.setDescription(resources.getString(R.string.downloading))
                                    request.setVisibleInDownloadsUi(true)
                                    request.setDestinationInExternalPublicDir(directory.absolutePath, file.absolutePath)



                                    refid = downloadManager.enqueue(request)

                                    listDownload.add(refid)
                                } else {
                                    //If permission is not present request for the same.
                                    EasyPermissions.requestPermissions(
                                        this@MainActivity,
                                        getString(R.string.write_file),
                                        WRITEREQUESTCODE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                }


                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    resources.getString(R.string.check_storage), Toast.LENGTH_LONG
                                ).show()

                            }

                        }
                    } catch (ex: Exception) {
                        Toast.makeText(
                            this@MainActivity, resources.getString(R.string.check_storage)
                            ,
                            Toast.LENGTH_LONG
                        ).show()
                    }


                } catch (e: Exception) {

                    Log.e("SEARCH_ERROR", e.message, e)
                }


            },
            Response.ErrorListener { error ->
                loading.stop()

                Log.e("SEARCH_ERROR", error.message, error)
                when (error) {
                    is NoConnectionError -> Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.check_internet),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    is TimeoutError -> Toast.makeText(
                        this@MainActivity, resources.getString(R.string.connection_timeout)
                        ,
                        Toast.LENGTH_LONG
                    ).show()

                    is ServerError -> Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.error_occured),
                        Toast.LENGTH_LONG
                    ).show()
                    is NetworkError -> Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.check_internet),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    is ParseError -> Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.error_occured),
                        Toast.LENGTH_LONG
                    ).show()
                    else -> Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.error_occured),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        emp3.getInstance().addToRequestQueue(postRequest, "SONG_REQUEST")

    }

    fun size(size: Long): String {
        val hrSize: String
        val m = size / 1000000.0
        val dec = DecimalFormat("0.00")

        hrSize = if (m > 1) {
            dec.format(m) + " mb"
        } else {
            dec.format(size / 1000) + " kb"
        }
        return hrSize
    }


    fun getSuggestion(searchTerm: String) {


        val url =
            "https://completion.amazon.com/search/complete?search-alias=digital-music&client=amazon-search-ui&" +
                    "mkt=1&callback=jQuery17208209149017895265_1540918003769&q=" + searchTerm + "&_=1540918046940"


        val postRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->


                Log.d("SEARCH_RESPONSE", response)


                try {
                    val songArray = response.substring(response.indexOf("["), response.indexOf("]")) + "]"
                    val n = searchTerm.length + 4
                    val finalArray = songArray.substring(n)
                    val arr = JSONArray(finalArray)
                    val list = ArrayList<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.get(i).toString())

                    }

                    val mAdapter: ArrayAdapter<String> =
                        ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, list)
                    txtSearch.threshold = 1
                    txtSearch.setAdapter(mAdapter)
                    mAdapter.notifyDataSetChanged()
                } catch (e: Exception) {

                    Log.e("SEARCH_ERROR", e.message, e)
                }
            },
            Response.ErrorListener { error ->
                Log.e("SEARCH_ERROR", error.message, error)
                if (error is NoConnectionError) {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.check_internet), Toast.LENGTH_LONG)
                        .show()

                } else if (error is NetworkError) {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.check_internet), Toast.LENGTH_LONG)
                        .show()

                }
            }
        )

        emp3.getInstance().addToRequestQueue(postRequest, "SONG_REQUEST")


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun getSongs() {

        avi.show()


        val url =
            "https://db.mp3.direct/api/monitor_api.cgi?jsoncallback=jQuery17204550281531570918_1540989912388&w=new&" +
                    "n=20&format=json&y=0434020b170e5f52455e51425b55455654425d544259554e5f5d&_=1540989955822"

        val postRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                avi.hide()

                Log.d("SONGS_RESPONSE", response)
                val song_array = response.substring(response.indexOf("["), response.indexOf("]")) + "]"
                Log.d("SONGS_ARRAY", song_array)

                try {
                    val jsonArray = JSONArray(song_array)

                    for (i in 0 until jsonArray.length()) {

                        val `object` = jsonArray.getJSONObject(i)

                        val song = `object`.getString("q")

                        songList.add(song)


                    }
                    val adapter = ArrayAdapter<String>(this, R.layout.list_item, songList)
                    list.adapter = adapter
                    list.setOnItemClickListener { parent, view, position, id ->


                        txtSearch.setText(list.getItemAtPosition(position).toString())


                        getSearchSongs(
                            "https://db.mp3.direct/api/search_api.cgi?jsoncallback=jQuery17208209149017895265_1540918003775&qry=" + list.getItemAtPosition(
                                position
                            ).toString() + "&format=json&mh=20&where=mdt&rel=new&r=&y=07350108160d5c53465d5c465d5d45595d445c534c5451465b51&_=1540918103693"
                        )


                    }
                } catch (e: JSONException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Something went wrong on our side. Please contact admin",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("CARGO_JSON_ERROR", e.message)
                }
            },
            Response.ErrorListener { error ->
                Log.e("emp3_ERROR", error.message, error)
                if (error is NoConnectionError) {
                    Toast.makeText(this@MainActivity, "Please check your internet connection", Toast.LENGTH_LONG)
                        .show()

                } else if (error is TimeoutError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Connection timed out.Please reload the page",
                        Toast.LENGTH_LONG
                    ).show()

                } else if (error is AuthFailureError) {
                    Toast.makeText(this@MainActivity, "Authorization failed.Please try again", Toast.LENGTH_LONG)
                        .show()
                } else if (error is ServerError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Server error.Please contact admin",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (error is NetworkError) {
                    Toast.makeText(this@MainActivity, "Please check your internet connection", Toast.LENGTH_LONG)
                        .show()

                } else if (error is ParseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "An error occurred on our side.Please contact admin",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "An error occurred.Please contact admin",
                        Toast.LENGTH_LONG
                    ).show()

                }
                avi.hide()
            }
        )

        emp3.getInstance().addToRequestQueue(postRequest, "SONG_REQUEST")
    }

    val onComplete = object : BroadcastReceiver() {

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun onReceive(ctxt: Context?, intent: Intent?) {


            val referenceId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            lateinit var t: String


            listDownload.remove(referenceId)


            if (listDownload.isEmpty()) {


                val intentX = Intent(applicationContext, MainActivity::class.java)
                intentX.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(
                    this@MainActivity, 1410,
                    intentX, PendingIntent.FLAG_ONE_SHOT
                )

                val query = DownloadManager.Query()
                query.setFilterById(referenceId!!)
                val c = downloadManager.query(query)
                if (c.moveToFirst()) {
                    t = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE))

                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationChannel =
                            NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
                    notificationChannel.enableLights(true)
                    notificationChannel.lightColor = Color.GREEN
                    notificationChannel.enableVibration(true)
                    notificationManager.createNotificationChannel(notificationChannel)



                    builder = Notification.Builder(this@MainActivity, channelId)
                        .setContentTitle(t)
                        .setSubText(resources.getString(R.string.download_complete))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setAutoCancel(false)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pendingIntent)


                } else {
                    builder = Notification.Builder(this@MainActivity)
                        .setContentTitle(t)
                        .setContentText(resources.getString(R.string.download_complete))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setAutoCancel(false)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)

                }



                notificationManager.notify(444, builder.build())


            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)

    }

    override fun onBackPressed() {


        AppRate.with(this)
            .setInstallDays(0) // default 10, 0 means install day.
            .setLaunchTimes(2) // default 10
            .setRemindInterval(2) // default 1
            .setShowLaterButton(true) // default true
            .setDebug(false) // default false
            .monitor()

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this)
        if (!AppRate.showRateDialogIfMeetsConditions(this)) {
            super.onBackPressed()
        }
    }
}

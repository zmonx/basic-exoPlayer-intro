/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
* limitations under the License.
 */
package com.example.exoplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.INotificationSideChannel
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.example.exoplayer.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util

/**
 * A fullscreen activity to play audio or video streams.
 */
class PlayerActivity : AppCompatActivity() {
    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private val playbackStateListener: Player.EventListener = playbackStateListener()
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
//        extractYoutubeUrl()
//        Log.e(TAG, "onCreate: "+  extractYoutubeUrl(), )
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        player = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                val youtubeLink = "https://www.youtube.com/watch?v=nsG0PGa6ToE"
                viewBinding.videoView.player = exoPlayer
                object : YouTubeExtractor(this) {
                    override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, vMeta: VideoMeta?) {
                        if (ytFiles != null) {
                            val videoUrl = ytFiles[137].url
                            val audioUrl = ytFiles[140].url
                            val audioSource: MediaSource = ProgressiveMediaSource
                                .Factory(DefaultHttpDataSourceFactory())
                                .createMediaSource(MediaItem.fromUri(audioUrl))
                            val videoSource: MediaSource = ProgressiveMediaSource
                                .Factory(DefaultHttpDataSourceFactory())
                                .createMediaSource(MediaItem.fromUri(videoUrl))
                            exoPlayer!!.setMediaSource(
                                MergingMediaSource(true, videoSource, audioSource),
                                true
                            )
                            exoPlayer!!.prepare()
                            exoPlayer!!.playWhenReady = playWhenReady
                            exoPlayer!!.seekTo(currentWindow, playbackPosition)
                            Log.e(TAG, "onExtractionComplete: success")

                        } else {
                            Log.e(TAG, "onExtractionComplete: error")
                        }
                    }
                }.extract(youtubeLink, true, true)
//                exoPlayer.seekTo(currentWindow, playbackPosition)
//                exoPlayer.addListener(playbackStateListener)
//                val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3))
//                val secondMediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4));
//                val ThridMediaItem = MediaItem.fromUri(getString(R.string.new_media_url_mp4));
//                exoPlayer.addMediaItem(mediaItem)
//                exoPlayer.addMediaItem(secondMediaItem)
//                exoPlayer.addMediaItem(ThridMediaItem)
////                exoPlayer.setMediaItem(mediaItem)
//                exoPlayer.playWhenReady = playWhenReady
//                exoPlayer.seekTo(currentWindow, playbackPosition)
//                exoPlayer.prepare()
            }
    }

    fun youtube() {
        player = SimpleExoPlayer.Builder(this).build()
        viewBinding.videoView.player = player
        val youtubeLink = "https://www.youtube.com/watch?v=nsG0PGa6ToE"
        object : YouTubeExtractor(this) {
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, vMeta: VideoMeta?) {
                if (ytFiles != null) {
                    val videoUrl = ytFiles[137].url
                    val audioUrl = ytFiles[140].url
                    val audioSource: MediaSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSourceFactory())
                        .createMediaSource(MediaItem.fromUri(audioUrl))
                    val videoSource: MediaSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSourceFactory())
                        .createMediaSource(MediaItem.fromUri(videoUrl))
                    player!!.setMediaSource(
                        MergingMediaSource(true, videoSource, audioSource),
                        true
                    )
                    player!!.prepare()
                    player!!.playWhenReady = playWhenReady
                    player!!.seekTo(currentWindow, playbackPosition)
                    Log.e(TAG, "onExtractionComplete: success")

                } else {
                    Log.e(TAG, "onExtractionComplete: error")
                }
            }
        }.extract(youtubeLink, false, true)

    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        viewBinding.videoView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        player = null
    }

    private fun playbackStateListener() = object : Player.EventListener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(TAG, "changed state to $stateString")
        }
    }

//    private fun extractYoutubeUrl() {
//        val id = viewBinding.videoView.player
//        val youtubeLink = "https://youtu.be/Aw9doej49ig"
//        object : YouTubeExtractor(this) {
//            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, vMeta: VideoMeta?) {
//                if (ytFiles != null) {
//
//                }
//            }
//        }.extract(youtubeLink,true,true)
//    }

    companion object {
        private const val TAG = "PlayerActivity"
    }
}
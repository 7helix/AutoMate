package com.automate.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri

data class SocialPlatform(
    val key: String,
    val label: String,
    val emoji: String,
    val pkg: String,
    val home: String,
    /** Profile URL template; {h} is replaced by the username. Empty = no profile links. */
    val profile: String
)

object Social {
    val all = listOf(
        SocialPlatform("instagram", "Instagram", "📸", "com.instagram.android", "https://instagram.com", "https://instagram.com/{h}"),
        SocialPlatform("whatsapp", "WhatsApp", "🟢", "com.whatsapp", "https://whatsapp.com", ""),
        SocialPlatform("youtube", "YouTube", "▶️", "com.google.android.youtube", "https://youtube.com", "https://youtube.com/@{h}"),
        SocialPlatform("x", "X", "🐦", "com.twitter.android", "https://x.com", "https://x.com/{h}"),
        SocialPlatform("facebook", "Facebook", "👍", "com.facebook.katana", "https://facebook.com", "https://facebook.com/{h}"),
        SocialPlatform("telegram", "Telegram", "✈️", "org.telegram.messenger", "https://telegram.org", "https://t.me/{h}"),
        SocialPlatform("tiktok", "TikTok", "🎵", "com.zhiliaoapp.musically", "https://tiktok.com", "https://tiktok.com/@{h}"),
        SocialPlatform("reddit", "Reddit", "👽", "com.reddit.frontpage", "https://reddit.com", "https://reddit.com/u/{h}"),
        SocialPlatform("linkedin", "LinkedIn", "💼", "com.linkedin.android", "https://linkedin.com", "https://linkedin.com/in/{h}"),
        SocialPlatform("snapchat", "Snapchat", "👻", "com.snapchat.android", "https://snapchat.com", "https://snapchat.com/add/{h}"),
        SocialPlatform("pinterest", "Pinterest", "📌", "com.pinterest", "https://pinterest.com", "https://pinterest.com/{h}"),
        SocialPlatform("twitch", "Twitch", "🎮", "tv.twitch.android.app", "https://twitch.tv", "https://twitch.tv/{h}"),
        SocialPlatform("discord", "Discord", "💬", "com.discord", "https://discord.com", ""),
        SocialPlatform("threads", "Threads", "🧵", "com.instagram.barcelona", "https://threads.net", "https://threads.net/@{h}")
    )

    @JvmStatic
    fun find(key: String): SocialPlatform? = all.firstOrNull { it.key == key }

    @JvmStatic
    fun url(p: SocialPlatform, handle: String): String {
        val h = handle.trim().removePrefix("@")
        return if (h.isEmpty() || p.profile.isEmpty()) p.home else p.profile.replace("{h}", h)
    }

    /** Opens the app if installed (or a profile link inside it); otherwise the website. */
    @JvmStatic
    fun buildIntent(ctx: Context, p: SocialPlatform, handle: String): Intent {
        val launch = ctx.packageManager.getLaunchIntentForPackage(p.pkg)
        val h = handle.trim()
        if (h.isEmpty() && launch != null) return launch
        val u = if (h.startsWith("http")) h else url(p, h)
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(u))
        if (launch != null) i.setPackage(p.pkg)
        return i
    }

    /** Opens a WhatsApp chat with the text typed in. The user still has to press send. */
    @JvmStatic
    fun whatsAppIntent(ctx: Context, phone: String, text: String): Intent {
        val digits = phone.filter { it.isDigit() }
        val u = "https://wa.me/$digits" + if (text.isBlank()) "" else "?text=" + Uri.encode(text)
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(u))
        if (ctx.packageManager.getLaunchIntentForPackage("com.whatsapp") != null) i.setPackage("com.whatsapp")
        return i
    }
}

fun shareText(ctx: Context, subject: String, text: String) {
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .putExtra(Intent.EXTRA_TEXT, text)
    ctx.startActivity(Intent.createChooser(send, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

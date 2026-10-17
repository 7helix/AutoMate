package com.automate.app.data

import android.content.Context

enum class DocCategory(val label: String, val emoji: String) {
    GUIDE("Guides", "📘"),
    LIMITS("Limitations", "🚧"),
    RULES("Rules & policies", "📜")
}

/** [asset] is a path inside the repo's docs/ folder, which is packaged into the app as assets. */
data class DocItem(
    val id: String,
    val category: DocCategory,
    val emoji: String,
    val title: String,
    val summary: String,
    val asset: String
)

object Docs {
    val all = listOf(
        DocItem("getting-started", DocCategory.GUIDE, "🚀", "Getting started", "Build your first rule in 60 seconds.", "guides/01-getting-started.md"),
        DocItem("triggers", DocCategory.GUIDE, "⏰", "Triggers explained", "Every “When” option and how it behaves.", "guides/02-triggers.md"),
        DocItem("actions", DocCategory.GUIDE, "🎬", "Actions explained", "Every “Then” option and its quirks.", "guides/03-actions.md"),
        DocItem("location-rules", DocCategory.GUIDE, "📍", "Location rules", "Arrive and leave rules done right.", "guides/04-location-rules.md"),
        DocItem("keep-alive", DocCategory.GUIDE, "🔋", "Keeping the engine running", "Stop your phone from killing AutoMate.", "guides/05-keep-engine-alive.md"),
        DocItem("recipes", DocCategory.GUIDE, "🍳", "Rule recipes", "Ready-to-copy ideas for every day.", "guides/06-recipes.md"),
        DocItem("social-automation", DocCategory.GUIDE, "📱", "Social media automation", "Reminders, shortcuts and share flows for your accounts.", "guides/08-social-automation.md"),
        DocItem("troubleshooting", DocCategory.GUIDE, "🛠️", "Troubleshooting", "A rule did not run? Start here.", "guides/07-troubleshooting.md"),
        DocItem("android-restrictions", DocCategory.LIMITS, "🔒", "Android restrictions", "What Android does not allow normal apps to do.", "limitations/01-android-restrictions.md"),
        DocItem("accuracy-and-battery", DocCategory.LIMITS, "🎯", "Accuracy, timing and battery", "Why things can be late or imprecise.", "limitations/02-accuracy-and-battery.md"),
        DocItem("not-supported", DocCategory.LIMITS, "🚫", "Not supported yet", "What AutoMate cannot do today.", "limitations/03-not-supported.md"),
        DocItem("social-limits", DocCategory.LIMITS, "🌐", "Social media limits", "What AutoMate can and cannot do with social apps.", "limitations/04-social-media-limits.md"),
        DocItem("rules-of-use", DocCategory.RULES, "⚖️", "Rules of use", "How AutoMate may and may not be used.", "rules/01-rules-of-use.md"),
        DocItem("privacy", DocCategory.RULES, "🛡️", "Privacy", "What is stored, what is not, and why each permission exists.", "rules/02-privacy.md"),
        DocItem("disclaimer-license", DocCategory.RULES, "📄", "Disclaimer and license", "No warranty, and the MIT license.", "rules/03-disclaimer-and-license.md")
    )

    fun find(id: String): DocItem? = all.firstOrNull { it.id == id }

    fun read(ctx: Context, asset: String): String =
        ctx.assets.open(asset).bufferedReader().use { it.readText() }
}

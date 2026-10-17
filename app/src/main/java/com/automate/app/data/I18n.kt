package com.automate.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Lang(val label: String, val code: String) {
    EN("English", "en"),
    HI("हिन्दी", "hi")
}

object I18n {
    /** Compose state: changing it refreshes every screen that calls tr(). */
    var lang by mutableStateOf(Lang.EN)
}

/** Translates a UI string. Unknown strings and English mode return the text unchanged. */
fun tr(en: String): String = if (I18n.lang == Lang.HI) (HI[en] ?: en) else en

private val HI: Map<String, String> = mapOf(
    // navigation and common
    "Home" to "होम", "Rules" to "नियम", "Activity" to "गतिविधि", "Learn" to "सीखें", "Settings" to "सेटिंग्स",
    "Cancel" to "रद्द करें", "OK" to "ठीक है", "Delete" to "हटाएँ", "Clear" to "साफ़ करें", "Back" to "वापस",
    "Save rule" to "नियम सहेजें", "Test action" to "एक्शन जाँचें", "Learn more" to "और जानें", "Allow" to "अनुमति दें",
    "Allowed ✓" to "अनुमति मिली ✓", "Skip" to "छोड़ें", "Next" to "आगे", "Get started" to "शुरू करें",
    "Share" to "शेयर करें", "Open" to "खोलें", "Export" to "निर्यात करें", "Import" to "आयात करें",
    // home
    "Your phone is ready to work for you." to "आपका फ़ोन आपके लिए काम करने को तैयार है।",
    "Engine is running" to "इंजन चालू है", "Engine is stopped" to "इंजन बंद है",
    "Watching battery, Wi-Fi, Bluetooth, location and more." to "बैटरी, वाई-फ़ाई, ब्लूटूथ, लोकेशन और बहुत कुछ पर नज़र रखी जा रही है।",
    "Tap the button to start automating." to "ऑटोमेशन शुरू करने के लिए बटन दबाएँ।",
    "Active" to "सक्रिय", "Runs today" to "आज के रन", "Quick start" to "जल्दी शुरू करें",
    "Recent activity" to "हाल की गतिविधि", "See all" to "सभी देखें", "Explore" to "खोजें",
    "Nothing has run yet. Create a rule and tap ▶ to test it." to "अभी तक कुछ नहीं चला। एक नियम बनाएँ और परखने के लिए ▶ दबाएँ।",
    "Social hub" to "सोशल हब", "Open your apps, share AutoMate, social recipes." to "अपने ऐप खोलें, AutoMate शेयर करें, सोशल रेसिपी।",
    "Learn & limitations" to "सीखें और सीमाएँ", "Guides, what Android won't allow, and the rules of use." to "गाइड, Android की सीमाएँ और उपयोग के नियम।",
    "Stats" to "आँकड़े", "Gallery" to "गैलरी", "Phone control" to "फ़ोन नियंत्रण", "Backup" to "बैकअप",
    // rules
    "Your rules" to "आपके नियम", "All" to "सभी", "Paused" to "रुके हुए", "New rule" to "नया नियम", "Edit rule" to "नियम संपादित करें",
    "No rules yet" to "अभी कोई नियम नहीं", "Tap “New rule” to automate your first task." to "अपना पहला काम ऑटोमेट करने के लिए “नया नियम” दबाएँ।",
    "Nothing here" to "यहाँ कुछ नहीं", "Try a different filter." to "कोई और फ़िल्टर आज़माएँ।", "Delete rule?" to "नियम हटाएँ?",
    "Never run yet" to "अभी तक नहीं चला", "Search rules" to "नियम खोजें", "Folders" to "फ़ोल्डर", "Unsorted" to "बिना फ़ोल्डर",
    // editor
    "Rule name" to "नियम का नाम", "Folder (optional)" to "फ़ोल्डर (वैकल्पिक)", "When this happens" to "जब यह हो",
    "Trigger" to "ट्रिगर", "Only if these are true (optional)" to "केवल तब जब ये सही हों (वैकल्पिक)",
    "Then do this" to "तब यह करें", "Add step" to "चरण जोड़ें", "Add condition" to "शर्त जोड़ें",
    "All must be true" to "सभी सही होनी चाहिए", "Any can be true" to "कोई एक सही हो", "Step" to "चरण",
    "Time (tap to change)" to "समय (बदलने के लिए दबाएँ)", "Message" to "संदेश", "Platform" to "प्लेटफ़ॉर्म",
    "Silent" to "साइलेंट", "Vibrate" to "वाइब्रेट", "Normal" to "सामान्य", "Condition" to "शर्त", "Action" to "एक्शन",
    "Choose an app" to "ऐप चुनें", "Search" to "खोजें", "Use my current location" to "मेरी वर्तमान लोकेशन उपयोग करें",
    "Latitude" to "अक्षांश", "Longitude" to "देशांतर", "From" to "से", "Until" to "तक",
    "No extra settings needed." to "अतिरिक्त सेटिंग की ज़रूरत नहीं।",
    // activity
    "No activity yet" to "अभी कोई गतिविधि नहीं", "When a rule runs, you'll see exactly what happened here." to "जब कोई नियम चलेगा, तो यहाँ दिखेगा कि क्या हुआ।",
    "Clear activity?" to "गतिविधि साफ़ करें?", "This removes the whole run history." to "इससे पूरा इतिहास हट जाएगा।",
    // learn
    "Search articles" to "लेख खोजें", "No matches" to "कोई परिणाम नहीं", "Try a different word or filter." to "कोई और शब्द या फ़िल्टर आज़माएँ।",
    "Keep reading" to "आगे पढ़ें", "Share article" to "लेख शेयर करें",
    // settings
    "Your name" to "आपका नाम", "What should I call you?" to "मैं आपको क्या कहकर बुलाऊँ?", "Language" to "भाषा",
    "Appearance" to "दिखावट", "Animations" to "एनिमेशन", "Smooth transitions, pulses and effects" to "सहज बदलाव और प्रभाव",
    "Haptic feedback" to "हैप्टिक फ़ीडबैक", "Light taps on buttons and switches" to "बटन और स्विच पर हल्का कंपन",
    "Toast notifications" to "टोस्ट सूचनाएँ", "How should quick messages appear?" to "छोटे संदेश कैसे दिखें?", "Preview toast" to "टोस्ट देखें",
    "Automation" to "ऑटोमेशन", "Start on boot" to "बूट पर शुरू करें", "Resume the engine after a restart" to "रीस्टार्ट के बाद इंजन फिर शुरू करें",
    "Permissions" to "अनुमतियाँ", "Data" to "डेटा", "Tools" to "टूल", "Help & policies" to "सहायता और नीतियाँ",
    "About AutoMate" to "AutoMate के बारे में", "Delete all rules" to "सभी नियम हटाएँ", "Clear activity" to "गतिविधि साफ़ करें",
    "Replay welcome tour" to "स्वागत टूर दोबारा देखें", "Backup & restore" to "बैकअप और रीस्टोर", "Statistics" to "आँकड़े",
    "What's new" to "नया क्या है", "Hindi covers menus, buttons and the main screens. Articles stay in English." to "हिन्दी में मेनू, बटन और मुख्य स्क्रीन हैं। लेख अंग्रेज़ी में रहेंगे।",
    "Notifications" to "सूचनाएँ", "Precise location" to "सटीक लोकेशन", "Display over other apps" to "अन्य ऐप के ऊपर दिखाएँ",
    "Exact alarms" to "सटीक अलार्म", "Unrestricted battery" to "बिना प्रतिबंध बैटरी", "Do Not Disturb access" to "डू नॉट डिस्टर्ब की पहुँच",
    "Modify system settings" to "सिस्टम सेटिंग बदलें", "Accessibility service" to "एक्सेसिबिलिटी सेवा",
    // stats
    "Runs this week" to "इस सप्ताह के रन", "Success rate" to "सफलता दर", "Day streak" to "लगातार दिन", "Time saved (estimate)" to "बचा हुआ समय (अनुमान)",
    "Last 7 days" to "पिछले 7 दिन", "Top rules" to "शीर्ष नियम", "No runs yet" to "अभी कोई रन नहीं",
    // gallery
    "Gallery" to "गैलरी", "Ready-made rules. Tap one to customise and save." to "तैयार नियम। अनुकूलित करने और सहेजने के लिए दबाएँ।", "Use this rule" to "यह नियम उपयोग करें",
    // backup
    "Export to file" to "फ़ाइल में निर्यात करें", "Import from file" to "फ़ाइल से आयात करें", "Share backup as text" to "बैकअप को टेक्स्ट के रूप में शेयर करें",
    // control
    "Accessibility is on" to "एक्सेसिबिलिटी चालू है", "Accessibility is off" to "एक्सेसिबिलिटी बंद है", "Open Accessibility settings" to "एक्सेसिबिलिटी सेटिंग खोलें",
    "Try it now" to "अभी आज़माएँ", "Open a settings page" to "सेटिंग पेज खोलें",
    // onboarding
    "Automate anything" to "कुछ भी ऑटोमेट करें", "Smart, not noisy" to "समझदार, शोर नहीं", "Make it yours" to "इसे अपना बनाएँ",
    "Build “when this happens → do that” rules for time, battery, Wi-Fi, Bluetooth and places." to "समय, बैटरी, वाई-फ़ाई, ब्लूटूथ और जगहों के लिए “जब यह हो → तब वह करो” नियम बनाएँ।",
    "Pick a trigger, pick an action. AutoMate runs quietly in the background and logs every run." to "एक ट्रिगर चुनें, एक एक्शन चुनें। AutoMate चुपचाप बैकग्राउंड में चलता है और हर रन दर्ज करता है।",
    "Material You, AMOLED black, colorful gradients or light. And tell me your name so I can greet you." to "Material You, AMOLED काला, रंगीन ग्रेडिएंट या लाइट। और अपना नाम बताइए ताकि मैं आपको नमस्ते कह सकूँ।",
    "Your name (optional)" to "आपका नाम (वैकल्पिक)",
    // toasts
    "Rule created" to "नियम बन गया", "Rule updated" to "नियम अपडेट हो गया", "Rule deleted" to "नियम हटा दिया गया",
    "All rules deleted" to "सभी नियम हटा दिए गए", "Activity log cleared" to "गतिविधि लॉग साफ़ हो गया",
    "Automation engine started ⚡" to "ऑटोमेशन इंजन शुरू हो गया ⚡", "Engine stopped" to "इंजन बंद हो गया",
    "Give your rule a name" to "अपने नियम को एक नाम दें", "Rule duplicated" to "नियम की प्रति बन गई",
    // enum labels
    "At a specific time" to "किसी निश्चित समय पर", "Repeat at an interval" to "हर कुछ अंतराल पर दोहराएँ", "Battery gets low" to "बैटरी कम हो जाए",
    "Charger connected" to "चार्जर लगाया गया", "Charger disconnected" to "चार्जर हटाया गया", "Battery saver turns on" to "बैटरी सेवर चालू हो",
    "Battery saver turns off" to "बैटरी सेवर बंद हो", "Wi-Fi connected" to "वाई-फ़ाई जुड़ा", "Wi-Fi disconnected" to "वाई-फ़ाई कटा",
    "Bluetooth connected" to "ब्लूटूथ जुड़ा", "Bluetooth disconnected" to "ब्लूटूथ कटा", "Airplane mode turned on" to "एयरप्लेन मोड चालू हुआ",
    "Airplane mode turned off" to "एयरप्लेन मोड बंद हुआ", "Wired headphones plugged in" to "तार वाले हेडफ़ोन लगाए गए",
    "Wired headphones unplugged" to "तार वाले हेडफ़ोन निकाले गए", "Screen turns on" to "स्क्रीन चालू हो", "Screen turns off" to "स्क्रीन बंद हो",
    "Phone unlocked" to "फ़ोन अनलॉक हुआ", "Phone starts up" to "फ़ोन चालू हो", "Arrive at a place" to "किसी जगह पहुँचें", "Leave a place" to "किसी जगह से निकलें",
    "Send a notification" to "सूचना भेजें", "Show a toast message" to "टोस्ट संदेश दिखाएँ", "Speak text aloud" to "टेक्स्ट बोलकर सुनाएँ",
    "Copy text to clipboard" to "टेक्स्ट कॉपी करें", "Vibrate the phone" to "फ़ोन वाइब्रेट करें", "Play a notification sound" to "सूचना की ध्वनि बजाएँ",
    "Change ringer mode" to "रिंगर मोड बदलें", "Set a volume level" to "वॉल्यूम स्तर तय करें", "Set screen brightness" to "स्क्रीन की चमक तय करें",
    "Change Do Not Disturb" to "डू नॉट डिस्टर्ब बदलें", "Open an app" to "ऐप खोलें", "Open a link" to "लिंक खोलें", "Open the camera" to "कैमरा खोलें",
    "Toggle flashlight" to "फ़्लैशलाइट चालू/बंद करें", "Press a system button" to "सिस्टम बटन दबाएँ", "Open Wi-Fi panel" to "वाई-फ़ाई पैनल खोलें",
    "Open a social app or profile" to "सोशल ऐप या प्रोफ़ाइल खोलें", "Open dialer with a number" to "नंबर के साथ डायलर खोलें",
    "Start a text message" to "टेक्स्ट संदेश शुरू करें", "Start an email" to "ईमेल शुरू करें", "Open WhatsApp chat with text" to "टेक्स्ट के साथ WhatsApp चैट खोलें",
    "Open share sheet with text" to "टेक्स्ट के साथ शेयर शीट खोलें",
    "Time is between" to "समय इनके बीच हो", "Day of the week is" to "सप्ताह का दिन हो", "Battery is above" to "बैटरी इससे ऊपर हो", "Battery is below" to "बैटरी इससे नीचे हो",
    "Phone is charging" to "फ़ोन चार्ज हो रहा हो", "Phone is not charging" to "फ़ोन चार्ज न हो रहा हो", "Wi-Fi is connected" to "वाई-फ़ाई जुड़ा हो",
    "Wi-Fi is not connected" to "वाई-फ़ाई जुड़ा न हो", "Connected to Wi-Fi named" to "इस नाम के वाई-फ़ाई से जुड़ा हो", "Airplane mode is on" to "एयरप्लेन मोड चालू हो",
    "Airplane mode is off" to "एयरप्लेन मोड बंद हो", "Screen is on" to "स्क्रीन चालू हो", "Do Not Disturb is on" to "डू नॉट डिस्टर्ब चालू हो",
    // more screens
    "About" to "परिचय", "Add to my rules" to "मेरे नियमों में जोड़ें", "Replace all" to "सभी बदलें",
    "Automate anything, anywhere" to "कहीं भी, कुछ भी ऑटोमेट करें",
    "Choose how to add them. Imported rules can run actions on your phone, so only continue if you trust this file." to "चुनें कि इन्हें कैसे जोड़ना है। आयात किए गए नियम आपके फ़ोन पर एक्शन चला सकते हैं, इसलिए केवल तभी आगे बढ़ें जब आप इस फ़ाइल पर भरोसा करते हों।",
    "Device name (empty = any device)" to "डिवाइस का नाम (खाली = कोई भी डिवाइस)",
    "Wi-Fi name (empty = any network)" to "वाई-फ़ाई का नाम (खाली = कोई भी नेटवर्क)", "Wi-Fi name" to "वाई-फ़ाई का नाम",
    "Disclaimer & license" to "अस्वीकरण और लाइसेंस", "Email address (optional)" to "ईमेल पता (वैकल्पिक)",
    "Good to know" to "जानने योग्य बातें", "How it works" to "यह कैसे काम करता है", "Limitations" to "सीमाएँ",
    "Link, e.g. example.com" to "लिंक, जैसे example.com", "Open page" to "पेज खोलें", "Phone number" to "फ़ोन नंबर",
    "Privacy policy" to "गोपनीयता नीति", "Project link (optional)" to "प्रोजेक्ट लिंक (वैकल्पिक)", "Read more" to "और पढ़ें",
    "Rules of use" to "उपयोग के नियम", "Share AutoMate" to "AutoMate शेयर करें", "Share app" to "ऐप शेयर करें",
    "Username or link (optional)" to "यूज़रनेम या लिंक (वैकल्पिक)",
    "⚖️  Rules of use" to "⚖️  उपयोग के नियम", "🚧  Limitations" to "🚧  सीमाएँ", "🛡️  Privacy" to "🛡️  गोपनीयता",
    "Wait" to "प्रतीक्षा करें", "Run right after the previous step" to "पिछले चरण के तुरंत बाद चलाएँ", "Button" to "बटन",
    "Create a rule and run it. Your numbers will appear here." to "एक नियम बनाकर चलाएँ। आपके आँकड़े यहाँ दिखेंगे।",
    // groups and categories
    "Time" to "समय", "Power" to "पावर", "Connectivity" to "कनेक्टिविटी", "Audio" to "ऑडियो", "Device" to "डिवाइस", "Place" to "जगह",
    "Alerts & messages" to "अलर्ट और संदेश", "Calls & messaging" to "कॉल और मैसेज", "Sound & display" to "ध्वनि और डिस्प्ले",
    "Apps & links" to "ऐप और लिंक", "Social" to "सोशल", "Everyday" to "रोज़मर्रा", "Sound & focus" to "ध्वनि और फ़ोकस", "Travel" to "यात्रा",
    // hints
    "Intervals under 10 minutes can be delayed while the phone is idle." to "10 मिनट से कम के अंतराल फ़ोन के निष्क्रिय रहने पर देर से चल सकते हैं।",
    "Wi-Fi names can only be read with location permission and location services on." to "वाई-फ़ाई के नाम तभी पढ़े जा सकते हैं जब लोकेशन अनुमति और लोकेशन सेवा चालू हो।",
    "Device names need the Nearby devices permission." to "डिवाइस के नाम के लिए नज़दीकी डिवाइस की अनुमति चाहिए।",
    "Only wired headphones are detected here. Bluetooth headphones use the Bluetooth trigger." to "यहाँ केवल तार वाले हेडफ़ोन पहचाने जाते हैं। ब्लूटूथ हेडफ़ोन के लिए ब्लूटूथ ट्रिगर उपयोग करें।",
    "This trigger needs the engine to be running (switch it on from Home)." to "इस ट्रिगर के लिए इंजन चालू होना चाहिए (होम से चालू करें)।",
    "Some phones block this until you open AutoMate once after a restart." to "कुछ फ़ोन रीस्टार्ट के बाद AutoMate एक बार खोले जाने तक इसे रोक देते हैं।",
    "Works best with location set to “Allow all the time”." to "लोकेशन को “हमेशा अनुमति दें” पर रखने पर सबसे अच्छा काम करता है।",
    "Android blocks background launches. Without “Display over other apps” you get a tap-to-open notification." to "Android बैकग्राउंड से ऐप खोलने पर रोक लगाता है। “अन्य ऐप के ऊपर दिखाएँ” की अनुमति के बिना आपको दबाकर खोलने वाली सूचना मिलेगी।",
    "Silent needs Do Not Disturb access." to "साइलेंट के लिए डू नॉट डिस्टर्ब की पहुँच चाहिए।",
    "Changing Do Not Disturb needs Do Not Disturb access." to "डू नॉट डिस्टर्ब बदलने के लिए उसकी पहुँच चाहिए।",
    "Brightness needs the “Modify system settings” permission." to "चमक बदलने के लिए “सिस्टम सेटिंग बदलें” की अनुमति चाहिए।",
    "System buttons need AutoMate's Accessibility service to be switched on." to "सिस्टम बटन के लिए AutoMate की एक्सेसिबिलिटी सेवा चालू होनी चाहिए।",
    "If you switch the flashlight elsewhere, the next toggle can get out of sync." to "यदि आप फ़्लैशलाइट कहीं और से बदलते हैं, तो अगला टॉगल उल्टा हो सकता है।",
    "Speech uses your phone's text-to-speech voice and the media volume." to "बोलने के लिए आपके फ़ोन की टेक्स्ट-टू-स्पीच आवाज़ और मीडिया वॉल्यूम उपयोग होता है।"
)

package com.freeyou.data

import java.util.Locale

object AdultBlockEngine {

    val MAJOR_ADULT_DOMAINS = hashSetOf(
        // The biggest tube networks
        "pornhub.com", "pornhubpremium.com", "xvideos.com", "xvideos2.com", "xnxx.com", "xnxx2.com",
        "xhamster.com", "xhamster.desi", "xhamster2.com", "xhamsterlive.com",
        "redtube.com", "youporn.com", "brazzers.com", "bangbros.com", "realitykings.com",
        "naughtyamerica.com", "mofos.com", "twistys.com", "digitalplayground.com", "rk.com",

        // Creator & subscription adult platforms
        "onlyfans.com", "fansly.com", "fapello.com", "coomer.party", "coomer.su", "kemono.party",
        "kemono.su", "manyvids.com", "loyalfans.com", "fanvue.com", "mym.fans", "clips4sale.com",
        "iwantclips.com", "modelhub.com", "admireme.vip", "justforfans.com", "pocketstars.com",

        // Cam & Live streaming adult networks
        "chaturbate.com", "stripchat.com", "bongacams.com", "livejasmin.com", "camsoda.com",
        "cam4.com", "flirt4free.com", "myfreecams.com", "streamate.com", "imlive.com",
        "camversity.com", "xcams.com", "babestation.tv",

        // Major free tube & aggregators
        "spankbang.com", "eporner.com", "hqporner.com", "beeg.com", "tube8.com", "fuq.com",
        "erome.com", "pornmd.com", "daftsex.com", "porndig.com", "txxx.com", "tubez.com",
        "drtuber.com", "nuvid.com", "shemale.xxx", "tnaflix.com", "empflix.com", "slutload.com",
        "sunporno.com", "pornrabbit.com", "pornsharing.com", "severeporn.com", "tubeoffline.com",
        "4tube.com", "fux.com", "porntube.com", "pornbox.com", "lubetube.com", "badjojo.com",
        "heavy-r.com", "motherless.com", "tblop.com", "freeomovie.com", "pornve.com",
        "pornone.com", "gotporn.com", "upornia.com", "porngo.com", "alphaporno.com",

        // Hentai, Manga & Anime 18+
        "nhentai.net", "tsumino.com", "hitomi.la", "hanime.tv", "rule34.xxx", "rule34.paheal.net",
        "gelbooru.com", "danbooru.donmai.us", "e-hentai.org", "exhentai.org", "hentaihaven.xxx",
        "hentaihaven.me", "fakku.net", "asmhentai.com", "simply-hentai.com", "luscious.net",
        "furaffinity.net", "e621.net", "pururin.io", "doujins.com", "hentai2read.com",

        // Dating & Hookup adult sites
        "adultfriendfinder.com", "ashleymadison.com", "fetlife.com", "alt.com", "passion.com",
        "fling.com", "casualx.badpuppy.com", "benaughty.com", "kasidie.com", "sdc.com",

        // Erotic literature & image boards
        "literotica.com", "lushstories.com", "bdsmlr.com", "vipergirls.to", "imagefap.com",
        "vintage-erotica-forum.com", "planetsuzy.org", "cyberdrop.me", "bunkr.is", "bunkr.ru"
    )

    // Keywords that MUST NEVER appear in any domain or host
    val ADULT_HOST_KEYWORDS = listOf(
        "pornhub", "xvideos", "xnxx", "xhamster", "onlyfans", "fansly", "fapello",
        "coomer", "kemono", "chaturbate", "stripchat", "camsoda", "bongacams",
        "livejasmin", "cam4", "redtube", "youporn", "brazzers", "bangbros", "rule34",
        "nhentai", "erome", "spankbang", "eporner", "hqporner", "daftsex", "xhamsterlive",
        "shemale", "hentai", "nsfw", "sexcam", "sexvid", "sexvideo", "adultwebcam",
        "porno", "porn", "xxx", "erotic", "nude", "nudes", "boobs", "dildo",
        "milf", "anal", "pussy", "blowjob", "fetlife", "literotica"
    )

    // TLDs reserved for 18+
    val ADULT_TLDS = listOf(".xxx", ".porn", ".adult", ".sex", ".sexy")

    // Keywords that trigger blocking when searched in browsers or viewed in apps (X, TikTok, Instagram, Reddit)
    val STRICT_CONTENT_TRIGGERS = listOf(
        // OnlyFans & creator variants
        "onlyfans", "only fans", "אונליפאנס", "אונלי פאנס", "fansly", "fapello",
        "leaked onlyfans", "onlyfans leak", "onlyfans free",

        // Core porn terms
        "pornhub", "xvideos", "xnxx", "xhamster", "redtube", "youporn", "brazzers",
        "chaturbate", "stripchat", "rule34", "nhentai", "erome", "spankbang",

        // Hebrew adult keywords
        "פורנו", "סרטי סקס", "סרטוני סקס", "אתרי סקס", "הורדות סקס", "סקס ישראלי",
        "תמונות עירום", "נערות ליווי", "זיונים", "זיון", "הנטאי", "סרטון סקס",

        // Explicit search patterns
        "free porn", "porn video", "sex video", "watch porn", "hd porn",
        "hardcore sex", "sex cam", "cam girl", "nudes in bio", "link in bio 🔞",
        "dm for nudes", "send nudes", "leaked nudes"
    )

    /**
     * Checks whether a URL, domain, or host represents adult content.
     * Returns the matched trigger word/host if blocked, or null if clean.
     */
    fun isUrlOrHostBlocked(rawUrl: String): String? {
        val trimmed = rawUrl.trim().lowercase(Locale.ROOT)
        if (trimmed.isEmpty()) return null

        // Strip protocol and path for host analysis
        val hostPart = trimmed
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore('/')
            .substringBefore(':')
            .removePrefix("www.")
            .trim()

        // 1. Direct domain match
        if (MAJOR_ADULT_DOMAINS.contains(hostPart)) {
            return hostPart
        }

        // 2. Subdomain check (e.g. video.pornhub.com -> pornhub.com)
        var sub = hostPart
        while (sub.contains('.')) {
            sub = sub.substringAfter('.')
            if (MAJOR_ADULT_DOMAINS.contains(sub)) {
                return hostPart
            }
        }

        // 3. Check adult TLDs
        for (tld in ADULT_TLDS) {
            if (hostPart.endsWith(tld)) return hostPart
        }

        // 4. Check host keywords (any domain with 'pornhub', 'onlyfans', 'xxx', 'porn')
        for (kw in ADULT_HOST_KEYWORDS) {
            if (hostPart.contains(kw)) {
                return hostPart
            }
        }

        // 5. Full URL path / query check (e.g. google.com/search?q=pornhub or twitter.com/hashtag/onlyfans)
        val normalizedFull = trimmed.replace("+", " ").replace("%20", " ")
        for (trigger in STRICT_CONTENT_TRIGGERS) {
            if (normalizedFull.contains(trigger)) {
                return trigger
            }
        }

        return null
    }

    /**
     * Checks visible screen text (from social apps like X, TikTok, Instagram, Reddit or browser pages)
     * for high-risk adult content triggers such as OnlyFans, Pornhub, porn keywords.
     */
    fun checkScreenText(text: CharSequence?): String? {
        if (text.isNullOrBlank()) return null
        val clean = text.toString().lowercase(Locale.ROOT).trim()

        // Check exact trigger occurrences
        for (trigger in STRICT_CONTENT_TRIGGERS) {
            if (clean.contains(trigger)) {
                return trigger
            }
        }

        // Check joined variant (e.g. "onlyfans.com" or "o n l y f a n s")
        val collapsed = clean.replace(" ", "").replace(".", "").replace("-", "")
        if (collapsed.contains("onlyfans") || collapsed.contains("אונליפאנס") ||
            collapsed.contains("pornhub") || collapsed.contains("xvideos") ||
            collapsed.contains("xnxx") || collapsed.contains("xhamster")
        ) {
            return "OnlyFans / 18+ Content"
        }

        return null
    }
}

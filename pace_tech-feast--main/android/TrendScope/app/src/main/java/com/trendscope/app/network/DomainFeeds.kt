package com.trendscope.app.network

object DomainFeeds {
    val FEEDS: Map<String, List<String>> = linkedMapOf(
        "AI" to listOf(
            "https://blog.google/technology/ai/rss/",
            "https://www.theverge.com/rss/ai-artificial-intelligence/index.xml",
        ),
        "Technology" to listOf(
            "https://feeds.arstechnica.com/arstechnica/index",
            "https://www.theverge.com/rss/index.xml",
            "https://www.zdnet.com/news/rss.xml",
        ),
        "Business" to listOf(
            "https://feeds.bbci.co.uk/news/business/rss.xml",
            "https://www.cnbc.com/id/100003114/device/rss/rss.html",
        ),
        "Finance" to listOf(
            "https://feeds.content.dowjones.io/public/rss/mw_topstories",
            "https://finance.yahoo.com/news/rssindex",
        ),
        "Health" to listOf(
            "https://rss.nytimes.com/services/xml/rss/nyt/Health.xml",
            "https://khn.org/feed/",
        ),
        "Education" to listOf(
            "https://rss.nytimes.com/services/xml/rss/nyt/Education.xml",
            "https://www.edsurge.com/feed.rss",
        ),
        "Entertainment" to listOf(
            "https://variety.com/feed/",
            "http://rss.cnn.com/rss/edition_entertainment.rss",
        ),
        "Sports" to listOf(
            "https://www.espn.com/espn/rss/news",
            "https://feeds.bbci.co.uk/sport/rss.xml",
        ),
        "World News" to listOf(
            "https://feeds.bbci.co.uk/news/world/rss.xml",
            "https://rss.nytimes.com/services/xml/rss/nyt/World.xml",
        ),
        "Science" to listOf(
            "https://www.sciencedaily.com/rss/all.xml",
            "https://rss.nytimes.com/services/xml/rss/nyt/Science.xml",
        ),
        "Cybersecurity" to listOf(
            "https://feeds.feedburner.com/TheHackersNews",
            "https://krebsonsecurity.com/feed/",
        ),
        "Gaming" to listOf(
            "https://www.polygon.com/rss/index.xml",
            "https://www.pcgamer.com/rss/",
        ),
        "Mobile & Gadgets" to listOf(
            "https://www.techradar.com/rss",
            "https://www.gsmarena.com/rss-news-reviews.php3",
        ),
        "Crypto" to listOf(
            "https://cointelegraph.com/rss",
            "https://coinrivet.com/feed/",
        ),
        "Environment" to listOf(
            "https://feeds.npr.org/1025/rss.xml",
            "https://rss.nytimes.com/services/xml/rss/nyt/Climate.xml",
        ),
        "Travel" to listOf(
            "https://www.rss-bridge.org/bridge01/?action=display&bridge=Nytimes&feed=travel",
            "https://www.thetravel.com/feed/",
        ),
        "Food" to listOf(
            "https://www.seriouseats.com/rss/all",
            "https://www.bonappetit.com/feed/rss",
        ),
        "Automobiles" to listOf(
            "https://www.autoblog.com/rss.xml",
            "https://www.caranddriver.com/rss/all.xml",
        ),
        "Defense" to listOf(
            "https://www.defenseone.com/rss/",
            "https://www.twz.com/rss",
        ),
        "Social Media" to listOf(
            "https://socialmediaexplorer.com/feed/",
            "https://www.socialmediatoday.com/rss/trending",
        ),
        "Politics" to listOf(
            "https://feeds.bbci.co.uk/news/politics/rss.xml",
            "http://rss.cnn.com/rss/cnn_allpolitics.rss",
        ),
        "Real Estate" to listOf(
            "https://www.inman.com/feed/",
            "https://www.housingwire.com/feed/",
        ),
        "Music" to listOf(
            "https://www.rollingstone.com/music/feed/",
            "https://pitchfork.com/feed/feed-news/rss",
        ),
        "Art & Design" to listOf(
            "https://www.artnews.com/feed/",
            "https://www.dezeen.com/feed/",
        ),
    )

    fun getAllCategories(): List<String> = FEEDS.keys.toList()

    fun getFeedsForCategory(category: String): List<String> = FEEDS[category] ?: emptyList()
}

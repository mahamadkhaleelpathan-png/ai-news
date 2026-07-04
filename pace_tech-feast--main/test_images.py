import requests

api_key = "063ddb9cdc89f579a86c0423bcdc1eb9"
url = "https://gnews.io/api/v4/search"

params = {
    "q": "AI",
    "lang": "en",
    "max": 5,
    "apikey": api_key
}

response = requests.get(url, params=params, timeout=30)
data = response.json()
articles = data.get("articles", [])

for i, a in enumerate(articles):
    title = a.get("title", "No title")
    image = a.get("image", "")
    print(f"[{i+1}] {title}")
    print(f"    Image URL: {image}")
    print()
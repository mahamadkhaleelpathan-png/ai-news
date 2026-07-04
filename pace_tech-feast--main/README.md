Here is a professional and complete `README.md` for your GitHub repository. You can copy and paste this directly into a file named `README.md` in the root of your project.

```markdown
# 🔭 TrendScope

**TrendScope** is an intelligent, real-time news aggregation dashboard designed to cut through the noise. It allows users to instantly fetch, filter, and read trending news across **20 unique domains** and **85+ sub-topics**—all from a clean, elegant interface.

Built for **Pace Techfeast '26**.

![TrendScope Light Mode](link-to-your-screenshot-light.png) 
*Add your own screenshots here*

## ✨ Features

- **20 Domains & 85+ Sub-Topics:** From Artificial Intelligence and Cybersecurity to Sports, Crypto, and Food Trends.
- **Real-Time Trending Feed:** Instantly loads top stories from 9 major domains on startup.
- **Advanced Search & Filtering:** Combine domain selection, sub-topics, and custom search queries with time-range filters (1 Day, 7 Days, 30 Days).
- **PDF Report Generation:** Export your filtered news feed into a clean, formatted PDF document with one click.
- **Dark/Light Theme Toggle:** Seamless theme switching with a smooth fade transition. Remembers your preference using `localStorage`.
- **Live Activity Log:** Real-time sidebar showing backend fetching status, errors, and article counts.
- **Zero API Keys Required:** Completely free to run, powered by public RSS feeds.

## 🛠️ Tech Stack

| Layer | Technology |
| --- | --- |
| **Backend** | Python, Flask |
| **Frontend** | HTML5, CSS3 (CSS Variables), Vanilla JavaScript |
| **Data Source** | Google News RSS (via `feedparser`) |
| **PDF Engine** | `fpdf2` |
| **Icons** | Iconify (Lucide Icons) |
| **Fonts** | Inter, Playfair Display |

## 📁 Project Structure

```text
Synapse-Daily-main/
│
├── app.py                 # Flask backend, API routes, RSS parsing, PDF generation
├── clean_layout.py        # Script to generate the frontend HTML/CSS/JS
├── requirements.txt       # Python dependencies
├── README.md              # You are here
│
└── templates/
    └── index.html         # Generated frontend file (Do not edit manually)
```

## 🚀 Getting Started

### Prerequisites
- Python 3.8 or higher installed.
- A terminal/command prompt.

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Synapse-Daily-main.git
   cd Synapse-Daily-main
   ```

2. **Create and activate a virtual environment:**
   *Windows (PowerShell):*
   ```powershell
   python -m venv .venv
   .venv\Scripts\Activate.ps1
   ```
   *macOS/Linux:*
   ```bash
   python3 -m venv .venv
   source .venv/bin/activate
   ```

3. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```
   *(If you don't have a requirements.txt yet, run: `pip install flask feedparser fpdf2`)*

4. **Generate the frontend:**
   ```bash
   python clean_layout.py
   ```
   *You should see a success message confirming the HTML was generated.*

5. **Run the application:**
   ```bash
   python app.py
   ```

6. **Open in browser:**
   Navigate to **http://localhost:5000**

## 🎨 Theme Toggle
Click the **Moon/Sun icon** in the top right corner of the header to switch between Light and Dark modes. Your choice is saved automatically and will persist across page refreshes.

## 🤝 Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## 📜 License
This project is open source and available under the [MIT License](LICENSE).
```

### 💡 Tips before you push:
1. **Create a `requirements.txt`** file in your project folder so anyone can easily install your dependencies. You can do this by running this in your terminal:
   ```powershell
   pip freeze > requirements.txt
   ```
2. **Add Screenshots:** Take a quick screenshot of the app in Light mode and Dark mode, upload them to your repo, and replace the `link-to-your-screenshot-light.png` text in the README with the actual GitHub link to those images. It makes the README look 10x better!
3. Make sure to replace `YOUR_USERNAME` in the clone link with your actual GitHub username.
"""Crawls the running app (http://localhost:8080) and writes a static copy of its pages next to this file.

GitHub Pages cannot run Spring Boot, so forms are switched off here: submitting one only shows a notice.
Usage: python export_static.py <repository-name>
"""
import pathlib
import posixpath
import re
import sys
import urllib.error
import urllib.request

BASE = "http://localhost:8080"
REPO = sys.argv[1]
OUT = pathlib.Path(__file__).parent

# Client staff named in the meeting protocol stay out of the public demo.
PEOPLE = {"Valeh Ağayev": "Kamran Əliyev", ">VA<": ">KƏ<", "Mahir Aliyev": "Tural Həsənov", ">MA<": ">TH<"}

NOTICE = """
<div id="static-notice" role="status" hidden style="position:fixed;left:50%;bottom:24px;transform:translateX(-50%);
max-width:calc(100% - 32px);padding:10px 16px;border-radius:9px;background:#1f2421;color:#fff;font-size:13.5px;z-index:10">
Bu, dizayn nümunəsidir. Dəyişikliklər yadda saxlanılmır.</div>
<script>
document.addEventListener('submit', event => {
    event.preventDefault();
    const notice = document.getElementById('static-notice');
    notice.hidden = false;
    clearTimeout(notice.timer);
    notice.timer = setTimeout(() => notice.hidden = true, 3000);
});
</script>
</body>"""


def fetch(route):
    request = urllib.request.Request(BASE + route, headers={"Accept": "text/html"})
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            return response.status, response.geturl(), response.read().decode("utf-8")
    except urllib.error.HTTPError as error:
        return error.code, BASE + route, error.read().decode("utf-8")


def target_dir(route):
    path, _, query = route.partition("?")
    path = path.strip("/")
    if query:
        path = posixpath.join(path, query.replace("=", "-"))
    return path


def relative(route, current_dir):
    if route.startswith("/css/"):
        return posixpath.relpath(route.lstrip("/"), current_dir or ".")
    link = posixpath.relpath(target_dir(route) or ".", current_dir or ".")
    return link + "/"


def anonymise(html):
    for name, sample in PEOPLE.items():
        html = html.replace(name, sample)
    return html.replace("Nümunə məlumatlar. Tətbiq yenidən başladıqda sıfırlanır.", "Dizayn nümunəsi. Məlumatlar uydurmadır.")


def rewrite(html, current_dir):
    html = re.sub(r'href="(/[^"]*)"', lambda m: f'href="{relative(m.group(1), current_dir)}"', html)
    html = re.sub(r'action="/[^"]*"', 'action="#"', html)
    return anonymise(html).replace("</body>", NOTICE)


queue, seen, skipped = ["/"], set(), []
while queue:
    route = queue.pop(0)
    if route in seen:
        continue
    seen.add(route)
    status, final_url, html = fetch(route)
    if status != 200 or final_url != BASE + route:
        skipped.append(f"{status} {route} -> {final_url}")
        continue
    queue.extend(link for link in re.findall(r'href="(/[^"#]*)"', html) if not link.startswith("/css/"))
    directory = target_dir(route)
    file = OUT / directory / "index.html"
    file.parent.mkdir(parents=True, exist_ok=True)
    file.write_text(rewrite(html, directory), encoding="utf-8")

css = urllib.request.urlopen(BASE + "/css/app.css").read()
(OUT / "css").mkdir(exist_ok=True)
(OUT / "css" / "app.css").write_bytes(css)

_, _, missing = fetch("/page-that-does-not-exist")
missing = re.sub(r'href="/css/', f'href="/{REPO}/css/', missing)
missing = re.sub(r'href="/"', f'href="/{REPO}/"', missing)
(OUT / "404.html").write_text(missing, encoding="utf-8")
(OUT / ".nojekyll").write_text("", encoding="utf-8")

print(f"pages: {len(seen) - len(skipped)}")
print("skipped:", *skipped, sep="\n  ")

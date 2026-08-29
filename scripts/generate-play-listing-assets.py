#!/usr/bin/env python3
"""Rasterize Play Store listing art from docs/logo.svg."""
from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

MOBILE = Path(__file__).resolve().parents[1]
ROOT = MOBILE.parent
LOGO = ROOT / "docs" / "logo.svg"
if not LOGO.is_file():
    LOGO = MOBILE / "assets" / "images" / "logo.svg"
OUT = MOBILE / "metadata" / "en-US" / "images"

WELL = (8, 9, 11)
PLATE = (20, 22, 27)
BRASS = (176, 141, 87)
MORTISE = (212, 175, 119)
INK = (246, 246, 246)
MUTED = (196, 184, 164)
STROKE = (42, 46, 54)

SERIF = "/usr/share/fonts/google-noto/NotoSerif-Regular.ttf"
SERIF_B = "/usr/share/fonts/google-noto/NotoSerif-Bold.ttf"
SANS = "/usr/share/fonts/liberation-sans-fonts/LiberationSans-Regular.ttf"
SANS_B = "/usr/share/fonts/liberation-sans-fonts/LiberationSans-Bold.ttf"


def font(path: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(path, size)


def raster_logo(px: int) -> Image.Image:
    with tempfile.TemporaryDirectory() as tmp:
        dest = Path(tmp) / "logo.png"
        subprocess.check_call(
            [
                "magick",
                "-background",
                "#08090b",
                str(LOGO),
                "-resize",
                f"{px}x{px}",
                str(dest),
            ]
        )
        return Image.open(dest).convert("RGBA")


def flatten(im: Image.Image, bg=WELL) -> Image.Image:
    base = Image.new("RGB", im.size, bg)
    if im.mode == "RGBA":
        base.paste(im, mask=im.split()[-1])
        return base
    return im.convert("RGB")


def wrap(draw: ImageDraw.ImageDraw, text: str, fnt, max_w: int) -> list[str]:
    words = text.split()
    lines: list[str] = []
    cur = ""
    for word in words:
        trial = f"{cur} {word}".strip()
        if draw.textlength(trial, font=fnt) <= max_w:
            cur = trial
        else:
            if cur:
                lines.append(cur)
            cur = word
    if cur:
        lines.append(cur)
    return lines


def write_centered(draw, xy, text, fnt, fill, max_w=None):
    x, y = xy
    if max_w:
        lines = wrap(draw, text, fnt, max_w)
    else:
        lines = [text]
    for i, line in enumerate(lines):
        w = draw.textlength(line, font=fnt)
        draw.text((x - w / 2, y + i * (fnt.size + 10)), line, font=fnt, fill=fill)


def phone_frame(canvas: Image.Image, screen: Image.Image, top: int) -> None:
    w, h = screen.size
    x = (canvas.size[0] - w) // 2
    bezel = 18
    frame = Image.new("RGB", (w + bezel * 2, h + bezel * 2 + 24), (18, 20, 24))
    draw = ImageDraw.Draw(frame)
    draw.rounded_rectangle(
        (0, 0, frame.size[0] - 1, frame.size[1] - 1),
        48,
        fill=(18, 20, 24),
        outline=BRASS,
        width=3,
    )
    frame.paste(flatten(screen, WELL), (bezel, bezel + 8))
    # home indicator
    d2 = ImageDraw.Draw(frame)
    mid = frame.size[0] // 2
    d2.rounded_rectangle(
        (mid - 70, frame.size[1] - 22, mid + 70, frame.size[1] - 12),
        6,
        fill=STROKE,
    )
    canvas.paste(frame, (x - bezel, top))


def ui_chrome(w: int, h: int, mark: Image.Image) -> Image.Image:
    im = Image.new("RGB", (w, h), WELL)
    d = ImageDraw.Draw(im)
    im.paste(flatten(mark.resize((56, 56), Image.Resampling.LANCZOS)), (28, 36))
    d.rounded_rectangle((100, 42, w - 88, 88), 16, fill=PLATE, outline=STROKE, width=2)
    d.text((118, 52), "Search vault…", font=font(SANS, 28), fill=MUTED)
    d.ellipse((w - 72, 46, w - 32, 86), outline=STROKE, width=2)
    return im


def shot_welcome(mark: Image.Image) -> Image.Image:
    w, h = 980, 1960
    im = Image.new("RGB", (w, h), WELL)
    d = ImageDraw.Draw(im)
    big = mark.resize((280, 280), Image.Resampling.LANCZOS)
    im.paste(flatten(big), ((w - 280) // 2, 420))
    write_centered(d, (w / 2, 760), "Welcome to", font(SERIF, 52), INK)
    write_centered(d, (w / 2, 830), "Lockwright", font(SERIF_B, 72), MORTISE)
    write_centered(
        d,
        (w / 2, 960),
        "Vaults stay on this device.",
        font(SANS, 32),
        MUTED,
        max_w=720,
    )
    d.rounded_rectangle((80, 1680, w - 80, 1800), 28, fill=BRASS)
    tw = d.textlength("Continue", font=font(SANS_B, 36))
    d.text(((w - tw) / 2, 1714), "Continue", font=font(SANS_B, 36), fill=WELL)
    return im


def shot_vault(mark: Image.Image) -> Image.Image:
    im = ui_chrome(980, 1960, mark)
    d = ImageDraw.Draw(im)
    chips = [("All", True), ("Logins", False), ("Cards", False), ("Notes", False)]
    x = 28
    for label, on in chips:
        fnt = font(SANS_B if on else SANS, 26)
        tw = d.textlength(label, font=fnt) + 48
        d.rounded_rectangle(
            (x, 120, x + tw, 176),
            20,
            fill=BRASS if on else PLATE,
            outline=BRASS if on else STROKE,
            width=2,
        )
        d.text(
            (x + 24, 132),
            label,
            font=fnt,
            fill=WELL if on else INK,
        )
        x += tw + 16
    rows = [
        ("GM", "Gmail", "work@lockwright"),
        ("GH", "GitHub", "thaoh"),
        ("BK", "Bank", "•••• 4421"),
        ("WF", "Wifi", "workshop"),
        ("NT", "Note", "Door codes"),
    ]
    y = 220
    for ini, title, sub in rows:
        d.rounded_rectangle((28, y, 952, y + 140), 20, fill=PLATE, outline=STROKE, width=1)
        d.rounded_rectangle((52, y + 32, 148, y + 108), 16, fill=WELL, outline=BRASS, width=2)
        iw = d.textlength(ini, font=font(SANS_B, 28))
        d.text((100 - iw / 2, y + 52), ini, font=font(SANS_B, 28), fill=MORTISE)
        d.text((176, y + 36), title, font=font(SANS_B, 34), fill=INK)
        d.text((176, y + 82), sub, font=font(SANS, 26), fill=MUTED)
        y += 156
    d.ellipse((430, 1760, 550, 1880), fill=BRASS)
    d.line((460, 1820, 520, 1820), fill=WELL, width=8)
    d.line((490, 1790, 490, 1850), fill=WELL, width=8)
    return im


def shot_create(mark: Image.Image) -> Image.Image:
    w, h = 980, 1960
    im = Image.new("RGB", (w, h), WELL)
    d = ImageDraw.Draw(im)
    small = mark.resize((72, 72), Image.Resampling.LANCZOS)
    im.paste(flatten(small), (40, 40))
    d.text((132, 52), "Lockwright", font=font(SERIF_B, 40), fill=INK)
    d.text((80, 180), "Name this vault", font=font(SERIF, 44), fill=INK)
    for y, label, value in (
        (320, "Vault name", "Workshop"),
        (500, "Master password", "••••••••••••"),
        (680, "Confirm password", "••••••••••••"),
    ):
        d.text((80, y), label, font=font(SANS, 26), fill=MUTED)
        d.rounded_rectangle((80, y + 40, 900, y + 140), 18, fill=PLATE, outline=STROKE, width=2)
        d.text((108, y + 70), value, font=font(SANS, 32), fill=INK)
    d.rounded_rectangle((80, 1680, 900, 1800), 28, fill=BRASS)
    tw = d.textlength("Create vault", font=font(SANS_B, 36))
    d.text(((w - tw) / 2, 1714), "Create vault", font=font(SANS_B, 36), fill=WELL)
    return im


def shot_sync(mark: Image.Image) -> Image.Image:
    im = ui_chrome(980, 1960, mark)
    d = ImageDraw.Draw(im)
    d.rounded_rectangle((60, 200, 920, 1680), 28, fill=PLATE, outline=STROKE, width=2)
    d.text((100, 240), "Add a device", font=font(SERIF_B, 40), fill=INK)
    d.text((100, 300), "This device's QR code", font=font(SANS, 26), fill=MUTED)
    # fake QR
    d.rounded_rectangle((250, 380, 730, 860), 8, fill=INK)
    d.rounded_rectangle((290, 420, 690, 820), 4, fill=WELL)
    for i in range(8):
        for j in range(8):
            if (i + j) % 2 == 0 or (i * j) % 3 == 0:
                d.rectangle(
                    (310 + i * 46, 440 + j * 46, 348 + i * 46, 478 + j * 46),
                    fill=MORTISE,
                )
    d.rounded_rectangle((140, 920, 840, 1000), 24, fill=WELL, outline=BRASS, width=2)
    d.text((220, 942), "Invite expires in 1:53", font=font(SANS, 28), fill=MORTISE)
    d.text((100, 1080), "Copy invite", font=font(SANS, 26), fill=MUTED)
    d.rounded_rectangle((100, 1124, 880, 1240), 16, fill=WELL, outline=STROKE, width=2)
    d.text((128, 1160), "lw://workshop/a8f3…c21", font=font(SANS, 28), fill=INK)
    d.rounded_rectangle((100, 1320, 880, 1560), 16, outline=MORTISE, width=2)
    write_centered(
        d,
        (490, 1360),
        "Treat this invite like the vault password.",
        font(SANS, 28),
        MUTED,
        max_w=720,
    )
    return im


def shot_generator(mark: Image.Image) -> Image.Image:
    w, h = 980, 1960
    im = Image.new("RGB", (w, h), WELL)
    d = ImageDraw.Draw(im)
    d.text((80, 80), "Generate a password", font=font(SERIF_B, 40), fill=INK)
    d.rounded_rectangle((80, 200, 900, 420), 20, fill=PLATE, outline=STROKE, width=2)
    d.text((110, 250), "k7#Qm2!vL9pR4wXe", font=font(SANS_B, 36), fill=MORTISE)
    d.text((110, 330), "Safe", font=font(SANS, 28), fill=BRASS)
    d.text((80, 500), "20 characters", font=font(SANS, 28), fill=MUTED)
    d.rounded_rectangle((80, 560, 900, 580), 8, fill=STROKE)
    d.rounded_rectangle((80, 552, 620, 588), 12, fill=BRASS)
    d.ellipse((592, 536, 648, 604), fill=MORTISE)
    for y, label, on in ((700, "Special characters", True), (820, "Digits", True)):
        d.text((80, y + 16), label, font=font(SANS, 32), fill=INK)
        d.rounded_rectangle((760, y + 8, 900, y + 64), 28, fill=BRASS if on else STROKE)
        cx = 864 if on else 796
        d.ellipse((cx - 22, y + 16, cx + 22, y + 60), fill=WELL)
    d.rounded_rectangle((80, 1680, 900, 1800), 28, fill=BRASS)
    tw = d.textlength("Use password", font=font(SANS_B, 36))
    d.text(((w - tw) / 2, 1714), "Use password", font=font(SANS_B, 36), fill=WELL)
    return im


def marketing(headline: str, sub: str, ui: Image.Image, mark: Image.Image) -> Image.Image:
    canvas = Image.new("RGB", (1242, 2688), WELL)
    glow = Image.new("RGB", canvas.size, WELL)
    gd = ImageDraw.Draw(glow)
    gd.ellipse((-200, 400, 700, 1400), fill=(40, 32, 20))
    glow = glow.filter(ImageFilter.GaussianBlur(80))
    canvas = Image.blend(canvas, glow, 0.55)
    d = ImageDraw.Draw(canvas)
    write_centered(d, (621, 80), headline, font(SERIF, 64), INK, max_w=1100)
    lines = wrap(d, headline, font(SERIF, 64), 1100)
    sub_y = 80 + len(lines) * 78 + 8
    write_centered(d, (621, sub_y), sub, font(SANS, 32), MUTED, max_w=1000)
    phone_frame(canvas, ui, top=sub_y + 140)
    return canvas


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    shots = OUT / "phoneScreenshots"
    shots.mkdir(exist_ok=True)
    mark = raster_logo(512)

    icon = flatten(mark.resize((512, 512), Image.Resampling.LANCZOS))
    icon.save(OUT / "icon.png", "PNG")

    feat = Image.new("RGB", (1024, 500), WELL)
    fd = ImageDraw.Draw(feat)
    logo = flatten(mark.resize((220, 220), Image.Resampling.LANCZOS))
    feat.paste(logo, (72, 140))
    fd.text((330, 160), "Lockwright", font=font(SERIF_B, 72), fill=INK)
    fd.text(
        (330, 270),
        "Peer-to-peer password manager",
        font=font(SANS, 32),
        fill=MUTED,
    )
    feat.save(OUT / "featureGraphic.png", "PNG")

    frames = [
        (
            "01.jpg",
            "Fully local",
            "Open-source password manager. No cloud account.",
            shot_welcome(mark),
        ),
        (
            "02.jpg",
            "Store more than passwords",
            "Logins, cards, identities, and notes. Encrypted on device.",
            shot_vault(mark),
        ),
        (
            "03.jpg",
            "Local storage",
            "Keep the vault off the cloud.",
            shot_vault(mark),
        ),
        (
            "04.jpg",
            "E2E encryption",
            "Only devices you pair can read it.",
            shot_create(mark),
        ),
        (
            "05.jpg",
            "Peer-to-peer sync",
            "Device to device. No intermediary.",
            shot_sync(mark),
        ),
        (
            "06.jpg",
            "Generate secure passwords",
            "Created and stored on this device.",
            shot_generator(mark),
        ),
    ]
    for name, title, sub, ui in frames:
        marketing(title, sub, ui, mark).save(shots / name, "JPEG", quality=90)

    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()

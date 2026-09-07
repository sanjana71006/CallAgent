import os
import math
from PIL import Image, ImageDraw, ImageFilter

def create_callmate_logo(size=512, is_round=False):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    center = size / 2.0
    radius = size * 0.46
    
    # 1. Base Gradient Canvas (Obsidian & Deep Cyber Violet)
    bg_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg_draw = ImageDraw.Draw(bg_img)
    
    for r in range(int(size * 0.75), 0, -2):
        factor = r / (size * 0.75)
        red = int(12 + factor * (38 - 12))
        green = int(18 + factor * (30 - 18))
        blue = int(45 + factor * (95 - 45))
        bg_draw.ellipse(
            (center - r, center - r, center + r, center + r),
            fill=(red, green, blue, 255)
        )
    
    # Clip background
    mask = Image.new("L", (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)
    if is_round:
        mask_draw.ellipse((center - radius, center - radius, center + radius, center + radius), fill=255)
    else:
        corner_radius = size * 0.22
        mask_draw.rounded_rectangle((size * 0.04, size * 0.04, size * 0.96, size * 0.96), radius=int(corner_radius), fill=255)
    
    bg_clipped = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg_clipped.paste(bg_img, (0, 0), mask=mask)
    img = Image.alpha_composite(img, bg_clipped)
    
    # 2. Glowing Outer Energy Rings
    glow_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_img)
    
    ring_radii = [size * 0.38, size * 0.30, size * 0.22]
    ring_colors = [
        (99, 102, 241, 70),   # Indigo glow
        (139, 92, 246, 110),  # Purple glow
        (0, 240, 255, 140),   # Cyan glow
    ]
    for r, col in zip(ring_radii, ring_colors):
        glow_draw.ellipse((center - r, center - r, center + r, center + r), outline=col, width=max(1, int(size * 0.015)))
    
    glow_blurred = glow_img.filter(ImageFilter.GaussianBlur(radius=size * 0.01))
    img = Image.alpha_composite(img, glow_blurred)
    draw = ImageDraw.Draw(img)

    # 3. Dynamic Center AI Wave Equalizer Bars
    bar_data = [
        (-size * 0.16, size * 0.08, (168, 85, 247)), # Purple
        (-size * 0.08, size * 0.14, (99, 102, 241)),  # Indigo
        (0,            size * 0.18, (0, 240, 255)),   # Cyan (Tallest)
        (size * 0.08,  size * 0.13, (16, 185, 129)),  # Emerald
        (size * 0.16,  size * 0.07, (0, 240, 255)),   # Cyan
    ]
    
    bar_width = size * 0.045
    bar_y_center = center + size * 0.03
    for x_off, h, col in bar_data:
        x0 = center + x_off - bar_width / 2
        x1 = center + x_off + bar_width / 2
        y0 = bar_y_center - h
        y1 = bar_y_center + h
        draw.rounded_rectangle((x0, y0, x1, y1), radius=max(1, int(bar_width / 2)), fill=(*col, 240))
    
    # 4. Sleek Modern Phone Handset
    ear_center = (center - size * 0.14, center - size * 0.18)
    ear_radius = size * 0.065
    draw.ellipse((ear_center[0] - ear_radius, ear_center[1] - ear_radius,
                  ear_center[0] + ear_radius, ear_center[1] + ear_radius), fill=(0, 240, 255, 255))
    draw.ellipse((ear_center[0] - ear_radius * 0.5, ear_center[1] - ear_radius * 0.5,
                  ear_center[0] + ear_radius * 0.5, ear_center[1] + ear_radius * 0.5), fill=(15, 23, 42, 255))
    
    mouth_center = (center - size * 0.14, center + size * 0.22)
    mouth_radius = size * 0.065
    draw.ellipse((mouth_center[0] - mouth_radius, mouth_center[1] - mouth_radius,
                  mouth_center[0] + mouth_radius, mouth_center[1] + mouth_radius), fill=(0, 240, 255, 255))
    draw.ellipse((mouth_center[0] - mouth_radius * 0.5, mouth_center[1] - mouth_radius * 0.5,
                  mouth_center[0] + mouth_radius * 0.5, mouth_center[1] + mouth_radius * 0.5), fill=(15, 23, 42, 255))

    bridge_x = center - size * 0.22
    draw.arc((bridge_x - size * 0.08, center - size * 0.20, bridge_x + size * 0.16, center + size * 0.24),
             start=90, end=270, fill=(0, 240, 255, 255), width=max(2, int(size * 0.06)))

    # 5. Radiant AI Intelligence Sparkle (✦)
    spark_center = (center + size * 0.22, center - size * 0.20)
    spark_size = size * 0.09
    
    points = [
        (spark_center[0], spark_center[1] - spark_size),
        (spark_center[0] + spark_size * 0.3, spark_center[1] - spark_size * 0.3),
        (spark_center[0] + spark_size, spark_center[1]),
        (spark_center[0] + spark_size * 0.3, spark_center[1] + spark_size * 0.3),
        (spark_center[0], spark_center[1] + spark_size),
        (spark_center[0] - spark_size * 0.3, spark_center[1] + spark_size * 0.3),
        (spark_center[0] - spark_size, spark_center[1]),
        (spark_center[0] - spark_size * 0.3, spark_center[1] - spark_size * 0.3),
    ]
    draw.polygon(points, fill=(255, 255, 255, 255))
    
    mini_center = (spark_center[0] + size * 0.09, spark_center[1] + size * 0.09)
    mini_size = size * 0.04
    mini_points = [
        (mini_center[0], mini_center[1] - mini_size),
        (mini_center[0] + mini_size * 0.3, mini_center[1] - mini_size * 0.3),
        (mini_center[0] + mini_size, mini_center[1]),
        (mini_center[0] + mini_size * 0.3, mini_center[1] + mini_size * 0.3),
        (mini_center[0], mini_center[1] + mini_size),
        (mini_center[0] - mini_size * 0.3, mini_center[1] + mini_size * 0.3),
        (mini_center[0] - mini_size, mini_center[1]),
        (mini_center[0] - mini_size * 0.3, mini_center[1] - mini_size * 0.3),
    ]
    draw.polygon(mini_points, fill=(0, 240, 255, 230))
    
    return img

def main():
    res_dir = r"c:\Users\sanju\OneDrive\Desktop\Agent Project\android\app\src\main\res"
    
    buckets = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    
    master_square = create_callmate_logo(size=512, is_round=False)
    master_round = create_callmate_logo(size=512, is_round=True)
    
    master_square.save(os.path.join(res_dir, "drawable", "callmate_logo_512.png"))
    
    for bucket_name, sz in buckets.items():
        bucket_dir = os.path.join(res_dir, bucket_name)
        os.makedirs(bucket_dir, exist_ok=True)
        
        sq_resized = master_square.resize((sz, sz), Image.Resampling.LANCZOS)
        sq_resized.save(os.path.join(bucket_dir, "ic_launcher.png"), "PNG")
        
        rd_resized = master_round.resize((sz, sz), Image.Resampling.LANCZOS)
        rd_resized.save(os.path.join(bucket_dir, "ic_launcher_round.png"), "PNG")
        
        print(f"Generated {bucket_name} ({sz}x{sz})")

if __name__ == "__main__":
    main()

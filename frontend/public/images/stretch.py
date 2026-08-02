from PIL import Image
import os

# Load the generated 1024x1024 image
img_path = r"C:\Users\Aditya\.gemini\antigravity-ide\brain\56114736-8486-412d-88eb-fdc34037abbd\shiva_1477_720_1785666502828.png"
img = Image.open(img_path).convert("RGB")

target_w, target_h = 1477, 720

# Resize image to match the target height (720x720) so no vertical cropping occurs
img_resized = img.resize((720, 720), Image.Resampling.LANCZOS)

# Create the final canvas
final = Image.new("RGB", (target_w, target_h))

# Calculate side padding
pad_left = (target_w - 720) // 2
pad_right = target_w - 720 - pad_left

# Stretch the 1-pixel outermost edge horizontally to create a smooth, seamless background extension
left_strip = img_resized.crop((0, 0, 1, 720)).resize((pad_left, 720))
right_strip = img_resized.crop((719, 0, 720, 720)).resize((pad_right, 720))

# Paste the seamless backgrounds and the center image
final.paste(left_strip, (0, 0))
final.paste(img_resized, (pad_left, 0))
final.paste(right_strip, (pad_left + 720, 0))

# Save directly overwriting the user's file
out_path = r"c:\Dhyan-app-claude\frontend\public\images\image.png"
final.save(out_path)
print(f"Successfully generated and formatted image to exactly {target_w}x{target_h}.")

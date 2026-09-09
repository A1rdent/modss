#!/usr/bin/env python3
"""
Generate textures for Companion Mod
Creates PNG textures for entity, item, and GUI
"""

import struct
import zlib
import os

def create_png(width, height, pixels, filepath):
    """Create a PNG file from pixel data (RGBA format)"""
    
    # PNG header
    png_header = b'\x89PNG\r\n\x1a\n'
    
    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data) & 0xffffffff
    ihdr_chunk = struct.pack('>I', 13) + b'IHDR' + ihdr_data + struct.pack('>I', ihdr_crc)
    
    # IDAT chunk (image data)
    raw_data = b''
    for y in range(height):
        raw_data += b'\x00'  # Filter type for each row
        for x in range(width):
            idx = (y * width + x) * 4
            raw_data += pixels[idx:idx+4]
    
    compressed = zlib.compress(raw_data, 9)
    idat_crc = zlib.crc32(b'IDAT' + compressed) & 0xffffffff
    idat_chunk = struct.pack('>I', len(compressed)) + b'IDAT' + compressed + struct.pack('>I', idat_crc)
    
    # IEND chunk
    iend_crc = zlib.crc32(b'IEND') & 0xffffffff
    iend_chunk = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', iend_crc)
    
    # Write PNG
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, 'wb') as f:
        f.write(png_header + ihdr_chunk + idat_chunk + iend_chunk)

def fill_rect(pixels, width, height, x, y, w, h, r, g, b, a):
    """Fill a rectangle with color"""
    for py in range(max(0, y), min(height, y + h)):
        for px in range(max(0, x), min(width, x + w)):
            idx = (py * width + px) * 4
            pixels[idx:idx+4] = bytes([r, g, b, a])

def draw_line(pixels, width, height, x1, y1, x2, y2, r, g, b, a, thickness=1):
    """Draw a line using Bresenham algorithm"""
    dx = abs(x2 - x1)
    dy = abs(y2 - y1)
    sx = 1 if x2 > x1 else -1
    sy = 1 if y2 > y1 else -1
    err = dx - dy
    
    x, y = x1, y1
    for _ in range(max(dx, dy) + 1):
        for tx in range(-thickness//2, thickness//2+1):
            for ty in range(-thickness//2, thickness//2+1):
                px, py = x + tx, y + ty
                if 0 <= px < width and 0 <= py < height:
                    idx = (py * width + px) * 4
                    pixels[idx:idx+4] = bytes([r, g, b, a])
        
        e2 = 2 * err
        if e2 > -dy:
            err -= dy
            x += sx
        if e2 < dx:
            err += dx
            y += sy

# 1. Create companion texture (64x64)
print("Creating companion texture (64x64)...")
pixels = bytearray(64 * 64 * 4)

# Background (sky blue)
for i in range(0, len(pixels), 4):
    pixels[i:i+4] = bytes([100, 150, 200, 255])

# Head (32x32 from 8,4)
fill_rect(pixels, 64, 64, 8, 4, 32, 32, 120, 160, 200, 255)
draw_line(pixels, 64, 64, 8, 4, 40, 4, 60, 80, 100, 255, 2)
draw_line(pixels, 64, 64, 8, 36, 40, 36, 60, 80, 100, 255, 2)
draw_line(pixels, 64, 64, 8, 4, 8, 36, 60, 80, 100, 255, 2)
draw_line(pixels, 64, 64, 40, 4, 40, 36, 60, 80, 100, 255, 2)

# Eyes
fill_rect(pixels, 64, 64, 14, 12, 4, 4, 0, 0, 0, 255)
fill_rect(pixels, 64, 64, 34, 12, 4, 4, 0, 0, 0, 255)
fill_rect(pixels, 64, 64, 15, 13, 2, 2, 200, 200, 255, 255)
fill_rect(pixels, 64, 64, 35, 13, 2, 2, 200, 200, 255, 255)

# Mouth
draw_line(pixels, 64, 64, 16, 24, 36, 24, 0, 0, 0, 255, 2)

# Body (32x24 from 12,36)
fill_rect(pixels, 64, 64, 12, 36, 32, 24, 100, 150, 200, 255)
draw_line(pixels, 64, 64, 12, 36, 44, 36, 60, 80, 100, 255, 2)
draw_line(pixels, 64, 64, 12, 60, 44, 60, 60, 80, 100, 255, 2)
draw_line(pixels, 64, 64, 12, 36, 12, 60, 60, 80, 100, 255, 2)
draw_line(pixels, 64, 64, 44, 36, 44, 60, 60, 80, 100, 255, 2)

# Arms
fill_rect(pixels, 64, 64, 4, 40, 8, 12, 110, 160, 210, 255)
fill_rect(pixels, 64, 64, 44, 40, 8, 12, 110, 160, 210, 255)

# Legs
fill_rect(pixels, 64, 64, 16, 56, 8, 8, 80, 130, 180, 255)
fill_rect(pixels, 64, 64, 32, 56, 8, 8, 80, 130, 180, 255)

create_png(64, 64, bytes(pixels), 
           "C:\\Users\\samp2\\MinecraftMods\\CompanionMod\\src\\main\\resources\\assets\\companionmod\\textures\\entity\\companion\\companion.png")
print("✅ Companion texture created!")

# 2. Create item texture (16x16)
print("Creating item texture (16x16)...")
pixels = bytearray(16 * 16 * 4)

# Background
for i in range(0, len(pixels), 4):
    pixels[i:i+4] = bytes([100, 150, 200, 255])

# Box
fill_rect(pixels, 16, 16, 2, 2, 12, 12, 200, 100, 50, 255)
draw_line(pixels, 16, 16, 2, 2, 14, 2, 100, 50, 0, 255, 1)
draw_line(pixels, 16, 16, 2, 14, 14, 14, 100, 50, 0, 255, 1)
draw_line(pixels, 16, 16, 2, 2, 2, 14, 100, 50, 0, 255, 1)
draw_line(pixels, 16, 16, 14, 2, 14, 14, 100, 50, 0, 255, 1)

# Circle/C shape
fill_rect(pixels, 16, 16, 6, 5, 4, 6, 255, 200, 100, 255)

create_png(16, 16, bytes(pixels),
           "C:\\Users\\samp2\\MinecraftMods\\CompanionMod\\src\\main\\resources\\assets\\companionmod\\textures\\item\\companion_spawner.png")
print("✅ Item texture created!")

# 3. Create GUI texture (176x222)
print("Creating GUI texture (176x222)...")
pixels = bytearray(176 * 222 * 4)

# Background
for i in range(0, len(pixels), 4):
    pixels[i:i+4] = bytes([140, 100, 60, 255])

# Border
for x in range(176):
    idx = 0 * 176 * 4 + x * 4
    pixels[idx:idx+4] = bytes([50, 30, 0, 255])
    idx = 221 * 176 * 4 + x * 4
    pixels[idx:idx+4] = bytes([50, 30, 0, 255])

for y in range(222):
    idx = y * 176 * 4 + 0 * 4
    pixels[idx:idx+4] = bytes([50, 30, 0, 255])
    idx = y * 176 * 4 + 175 * 4
    pixels[idx:idx+4] = bytes([50, 30, 0, 255])

# Header
fill_rect(pixels, 176, 222, 8, 8, 160, 22, 100, 70, 40, 255)

# Buttons (Mine, Follow, Gather, Stop)
buttons = [(16, 50), (92, 50), (16, 100), (92, 100)]
for bx, by in buttons:
    fill_rect(pixels, 176, 222, bx, by, 60, 35, 100, 150, 200, 255)
    draw_line(pixels, 176, 222, bx, by, bx+60, by, 50, 100, 150, 255, 2)
    draw_line(pixels, 176, 222, bx, by+35, bx+60, by+35, 50, 100, 150, 255, 2)
    draw_line(pixels, 176, 222, bx, by, bx, by+35, 50, 100, 150, 255, 2)
    draw_line(pixels, 176, 222, bx+60, by, bx+60, by+35, 50, 100, 150, 255, 2)

# Inventory grid (9x4 = 36 slots)
for row in range(4):
    for col in range(9):
        sx = 12 + col * 18
        sy = 150 + row * 18
        fill_rect(pixels, 176, 222, sx, sy, 16, 16, 80, 80, 80, 255)
        draw_line(pixels, 176, 222, sx, sy, sx+16, sy, 30, 30, 30, 255, 1)
        draw_line(pixels, 176, 222, sx, sy+16, sx+16, sy+16, 30, 30, 30, 255, 1)
        draw_line(pixels, 176, 222, sx, sy, sx, sy+16, 30, 30, 30, 255, 1)
        draw_line(pixels, 176, 222, sx+16, sy, sx+16, sy+16, 30, 30, 30, 255, 1)

create_png(176, 222, bytes(pixels),
           "C:\\Users\\samp2\\MinecraftMods\\CompanionMod\\src\\main\\resources\\assets\\companionmod\\textures\\gui\\companion_gui.png")
print("✅ GUI texture created!")

print("\n🎨 All textures created successfully!")

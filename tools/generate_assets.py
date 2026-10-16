"""
Draws the textures for Caffeine Cubed: the coffee bean and grounds icons for each roast, the cups of Coffee, Ice Coffee and God's Coffee, the guide book (its icon, its open
pages and their buttons, in the layout Patchouli reads), the
Coffee Bush's four growth stages, the Rainbow Blend, the Coffee Roaster's, Coffee Machine's and Triple Axis Siphon's
interfaces and the JEI pages. It only
creates textures that don't exist yet, so textures redrawn by hand are never overwritten; delete a texture to have it
drawn again. The Coffee Roaster's, Coffee Grinder's, Coffee Machine's, attachments' and Triple Axis
Siphon's own models and textures are made in Blockbench, so this script leaves them alone.

Run from the project root:   python tools/generate_assets.py
Needs Pillow (pip install pillow). Add --preview <file.png> to also save an enlarged preview sheet.

The bush is drawn like vanilla's sweet berry bush (a small starter bush, then a full bush), with glossy coffee
leaves and cherries that ripen from green to red. The roaster's interface is a warm cafe: a dark green menu board,
a cream wall lit by copper pendant lamps, and a wooden counter.
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

PROJECT = Path(__file__).resolve().parent.parent
ASSETS = PROJECT / "src/main/resources/assets/caffeinecubed"
TEXTURES = ASSETS / "textures"

# Single characters are used in the pixel maps below
COLORS = {
    ".": None,
    # Raw (green) coffee beans
    "a": "#d3d79a",  # shine
    "b": "#b3b977",
    "c": "#8e9757",
    "d": "#646c3a",  # crease and shadow
    # Coffee bush
    "t": "#6b4a2b",  # stem
    "T": "#4b321c",  # stem in shadow
    "G": "#6cbf4f",  # leaf shine
    "g": "#3f8f36",  # leaf
    "h": "#2c6a2a",  # leaf in shadow
    "H": "#1d4a20",  # leaf, deep shadow
    "u": "#dde784",  # unripe cherry shine
    "U": "#a9c44a",  # unripe cherry
    "p": "#ff7a63",  # ripe cherry shine
    "q": "#d0302a",  # ripe cherry
    "x": "#8c1a17",  # ripe cherry in shadow
}

# The four shades of each roast (shine, base, shadow, crease), swapped in for the raw bean's a, b, c and d
ROASTS = {
    "light_roast": ("#d4a46a", "#b07a45", "#8a5a2e", "#5c3517"),
    "medium_roast": ("#a26a3c", "#7c4a26", "#5c3418", "#36190a"),
    "dark_roast": ("#7a5642", "#3f2618", "#2b170c", "#120804"),
}

# A cup of Coffee: a white takeaway cup with a lid and a kraft paper sleeve
CUP_COLOURS = {
    "a": "#fbf8f2",  # lid, lit
    "b": "#e0d9cc",  # lid
    "h": "#5a4a40",  # sip hole
    "r": "#9c9184",  # lid rim
    "o": "#6d625a",  # outline
    "C": "#f6f2ea",  # cup
    "c": "#d8cfc1",  # cup in shadow
    "S": "#b98a57",  # sleeve
    "s": "#94693f",  # sleeve in shadow
}
CUP = [
    "......oooo......",
    ".....oahabo.....",
    "...oaaaaaaabo...",
    "...rbbbbbbbbr...",
    "...rrrrrrrrrr...",
    "....oCCCCCco....",
    "....oCCCCCco....",
    "....oCCCCCco....",
    "....oSSSSSso....",
    "....oSSSSSso....",
    ".....oSSSso.....",
    ".....oSSSso.....",
    ".....oCCCco.....",
    ".....oCCCco.....",
    ".....oCCcco.....",
    "......oooo......",
]

# A cup of Ice Coffee: a clear plastic cup, narrower at the bottom, with a flat lid and a green straw, showing the
# coffee and the ice cubes floating in it
ICED_CUP_COLOURS = {
    "G": "#6fcf7f",  # straw, lit
    "g": "#2e8b47",  # straw
    "L": "#f4f9fb",  # lid, lit
    "l": "#cddde4",  # lid
    "r": "#8fa7b3",  # lid rim
    "o": "#7d9aa8",  # the cup's clear edge
    "w": "#dcedf4",  # glint on the plastic
    "k": "#6b3a1a",  # coffee
    "K": "#4a2510",  # coffee in shadow
    "s": "#2f5a35",  # the straw, seen through the coffee
    "i": "#f1fafd",  # ice
    "I": "#b4d8e6",  # ice in shadow
}
ICED_CUP = [
    "........Gg......",
    "........Gg......",
    ".oLLLLLLGgLLLLo.",
    ".rllllllllllllr.",
    "..owiikkksiiko..",
    "..owiIkkksiIko..",
    "..owkkiikskkKo..",
    "..owkkiIksKkKo..",
    "...owkkkksiio...",
    "...owKkkksiIo...",
    "...owiikkskKo...",
    "...owiIkKskKo...",
    "....owkiisKo....",
    "....owkiIsKo....",
    "....owKKKKKo....",
    ".....oooooo.....",
]

# One bean with its crease; the icon scatters three of them
BEAN = [
    "..bbc.",
    ".baabc",
    "baadbc",
    "babdbc",
    "bbdbcc",
    "bbdbcd",
    ".cdccd",
    "..ddd.",
]
BEAN_SPOTS = [(1, 0), (9, 3), (3, 8)]

STARTER_BUSH = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "......gG..G.....",
    "....hGgh.gGgh...",
    "...hgGgGhgGghh..",
    "...HhgghgGghgh..",
    "...hgGhgtgGhgH..",
    "....HhhtThhH....",
    ".....H.tT.H.....",
]

BUSH = [
    "................",
    "......gG..Gg....",
    "...hGghgGhgGgh..",
    "..hgGgghGgghgGh.",
    ".hgGhgGgghgGghh.",
    ".hggGhgggGhggGgh",
    "hgGgghGghggGhgh.",
    ".hhgGggGhgGgghg.",
    "hgGghggGgghGggh.",
    ".hgggGhgGggGghH.",
    "hhgGgghgghgGghh.",
    ".hgGhggGhhgGghh.",
    "hHhghgGhghggHhH.",
    ".hHgGhtTgGhhgH..",
    "..HhhHtThhHhH...",
    "...H..tT..H.....",
]

# Where the cherry clusters sit on the bush (top-left corner of each 2x2 cluster)
CHERRY_SPOTS = [(4, 2), (10, 3), (2, 6), (12, 7), (4, 10), (10, 10)]
UNRIPE = ["uU", "UU"]
RIPE = ["pq", "qx"]


def scatter_beans():
    grid = [["."] * 16 for _ in range(16)]
    for left, top in BEAN_SPOTS:
        for y, row in enumerate(BEAN):
            for x, ch in enumerate(row):
                if ch != ".":
                    grid[top + y][left + x] = ch
    return ["".join(row) for row in grid]


def scatter_grounds_rows():
    """The shape of the grounds heap as a character map."""
    img = grounds_pile(ROASTS["medium_roast"])
    return ["".join("#" if img.getpixel((x, y))[3] else "." for x in range(16)) for y in range(16)]


def grounds_pile(shades):
    """A little heap of ground coffee in a roast's four shades: lit from the top left, speckled, darker at the edges."""
    shine, base, shadow, edge = (hex_rgba(shade) for shade in shades)
    img = Image.new("RGBA", (16, 16))
    tops = {}
    for x in range(1, 15):
        spread = abs(x - 7.5) / 7.0
        tops[x] = 14 - round(9 * (1 - spread ** 2) ** 0.9)
    for x, top in tops.items():
        for y in range(top, 15):
            outline = y == top or y == 14 or x in (1, 14) or tops.get(x - 1, 15) > y or tops.get(x + 1, 15) > y
            # Deterministic grains, so the texture comes out the same every time
            grain = (x * 7 + y * 13 + x * y) % 9
            lit = (x - 7.5) + (y - top) * 0.6 < 0
            if outline:
                colour = edge
            elif grain == 0:
                colour = shine
            elif grain in (4, 7):
                colour = shadow
            else:
                colour = shine if lit and grain in (2, 5) else base
            img.putpixel((x, y), colour)
    return img


def with_cherries(rows, cluster):
    grid = [list(row) for row in rows]
    for left, top in CHERRY_SPOTS:
        for y, row in enumerate(cluster):
            for x, ch in enumerate(row):
                grid[top + y][left + x] = ch
    return ["".join(row) for row in grid]


def rgba(name):
    value = COLORS[name]
    return (0, 0, 0, 0) if value is None else tuple(int(value[i:i + 2], 16) for i in (1, 3, 5)) + (255,)


def draw_map(rows, shades=None):
    """Paints a 16x16 character map. shades replaces the bean colours a, b, c and d, for the roasted beans."""
    swap = dict(zip("abcd", shades)) if shades else {}
    img = Image.new("RGBA", (16, 16))
    for y, row in enumerate(rows):
        assert len(row) == 16, row
        for x, ch in enumerate(row):
            img.putpixel((x, y), hex_rgba(swap[ch]) if ch in swap else rgba(ch))
    return img


def hex_rgba(value):
    return tuple(int(value[i:i + 2], 16) for i in (1, 3, 5)) + (255,)


# --- the Coffee Roaster's interface ---

GUI = {
    "frame": "#24170f",       # espresso-dark outline
    "board": "#1e3932",       # green menu board
    "board_light": "#2f5248",
    "board_dark": "#132520",
    "wall": "#ecdcc3",        # cream wall
    "wall_light": "#f7ecdb",
    "wall_dark": "#d9c4a5",
    "trim": "#5a3a22",        # dark wood trim between the wall and the counter
    "plank_gap": "#5e3c24",
    "grain": "#7b4f2f",
    "grain_light": "#b8865a",
    "slot": "#a88a6e",
    "slot_dark": "#4a3020",
    "slot_light": "#ead6b8",
    "copper": "#c27a45",
    "copper_light": "#e8a774",
    "copper_dark": "#7f4824",
    "cord": "#2a1d15",
    "glow": "#fff1c4",
    "ghost": "#d4bd9c",       # the empty flame and arrow on the wall
    "flame_tip": "#ffd36b",
    "flame": "#f59a3a",
    "flame_base": "#c9561e",
}
PLANKS = ["#9a6a42", "#8d5f3a", "#a3714a", "#8a5b37", "#966641"]
WALL_BOTTOM = 67   # the cream wall ends here; the wooden counter starts below the trim
FLAME = [
    "......#.......",
    ".....##.......",
    ".....###......",
    "....####...#..",
    "..#.#####..##.",
    "..#######.###.",
    ".############.",
    ".############.",
    ".############.",
    "..###########.",
    "..##########..",
    "...########...",
    "....######....",
    ".....####.....",
]
# The arrow's colour runs from a light roast to a dark one as it fills
ARROW_ROAST = ["#d8a56a", "#c48b52", "#a8703d", "#8c582d", "#6e4221", "#4f2d15"]


def arrow_mask(x, y):
    """A 24x16 arrow pointing right."""
    if x < 14:
        return 6 <= y <= 9
    return abs(y + 0.5 - 8) <= (24 - x) * 0.8


def gui_slot(draw, x, y, size=18):
    """A slot sunk into the wall: shadowed at the top left, lit at the bottom right."""
    draw.rectangle((x, y, x + size - 1, y + size - 1), fill=hex_rgba(GUI["slot"]))
    draw.line((x, y, x + size - 2, y), fill=hex_rgba(GUI["slot_dark"]))
    draw.line((x, y, x, y + size - 2), fill=hex_rgba(GUI["slot_dark"]))
    draw.line((x + 1, y + size - 1, x + size - 1, y + size - 1), fill=hex_rgba(GUI["slot_light"]))
    draw.line((x + size - 1, y + 1, x + size - 1, y + size - 1), fill=hex_rgba(GUI["slot_light"]))


def copper_slot(draw, x, y):
    """The big 26x26 slot roasted beans come out into, in a copper frame. x and y are the outer frame's corner."""
    draw.rectangle((x, y, x + 29, y + 29), outline=hex_rgba(GUI["copper_dark"]))
    draw.rectangle((x + 1, y + 1, x + 28, y + 28), outline=hex_rgba(GUI["copper"]))
    gui_slot(draw, x + 2, y + 2, 26)


def ghost_flame_and_arrow(img, flame_x, flame_y, arrow_x, arrow_y):
    """The empty flame and arrow, which fill in as the roaster works."""
    for y, row in enumerate(FLAME):
        for x, ch in enumerate(row):
            if ch == "#":
                img.putpixel((flame_x + x, flame_y + y), hex_rgba(GUI["ghost"]))
    for y in range(16):
        for x in range(24):
            if arrow_mask(x, y):
                img.putpixel((arrow_x + x, arrow_y + y), hex_rgba(GUI["ghost"]))


def jei_roasting_panel():
    """
    JEI's Coffee Roasting page, 88x44: a strip of the roaster's cream wall over its wooden counter, with the bean slot,
    flame, arrow and copper-framed output. RoastingCategory uses the same positions.
    """
    width, height = 88, 44
    img = Image.new("RGBA", (width, height))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, width - 1, height - 1), fill=hex_rgba(GUI["wall"]))
    for y in range(1, 37, 3):
        for x in range(3 + (y % 2) * 5, width - 3, 11):
            draw.point((x, y), fill=hex_rgba(GUI["wall_light"]))
    draw.line((0, 38, width - 1, 38), fill=hex_rgba(GUI["trim"]))
    x, plank = 0, 0
    while x < width:
        plank_width = 14 + (plank * 5) % 7
        draw.rectangle((x, 39, x + plank_width - 1, height - 1), fill=hex_rgba(PLANKS[plank % len(PLANKS)]))
        draw.line((x, 39, x, height - 1), fill=hex_rgba(GUI["plank_gap"]))
        x += plank_width
        plank += 1
    gui_slot(draw, 2, 2)
    ghost_flame_and_arrow(img, 4, 23, 26, 12)
    copper_slot(draw, 56, 6)
    return img


def jei_grinding_panel():
    """
    JEI's Coffee Grinding page, 88x50: the roasting page's wall and counter, with the beans' slot above the grinder's,
    an arrow and the copper-framed grounds slot. GrindingCategory uses the same positions.
    """
    width, height = 88, 50
    img = Image.new("RGBA", (width, height))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, width - 1, height - 1), fill=hex_rgba(GUI["wall"]))
    for y in range(1, 43, 3):
        for x in range(3 + (y % 2) * 5, width - 3, 11):
            draw.point((x, y), fill=hex_rgba(GUI["wall_light"]))
    draw.line((0, 44, width - 1, 44), fill=hex_rgba(GUI["trim"]))
    x, plank = 0, 0
    while x < width:
        plank_width = 14 + (plank * 5) % 7
        draw.rectangle((x, 45, x + plank_width - 1, height - 1), fill=hex_rgba(PLANKS[plank % len(PLANKS)]))
        draw.line((x, 45, x, height - 1), fill=hex_rgba(GUI["plank_gap"]))
        x += plank_width
        plank += 1
    gui_slot(draw, 2, 2)
    gui_slot(draw, 2, 26)
    for y in range(16):
        for x in range(24):
            if arrow_mask(x, y):
                img.putpixel((26 + x, 14 + y), hex_rgba(GUI["ghost"]))
    copper_slot(draw, 56, 8)
    return img


def cafe_panel(height, wall_bottom, lamps=(24, 151)):
    """
    The background every interface shares, 176 wide: a green menu board for the title over a cream wall lit by copper
    pendant lamps (two unless told otherwise), and a wooden counter from wall_bottom down for the player's inventory.
    """
    img = Image.new("RGBA", (256, 256))
    draw = ImageDraw.Draw(img)

    def c(name):
        return hex_rgba(GUI[name])

    width = 176

    # Wooden counter: vertical planks with a little grain, filling everything below the wall
    x, plank = 0, 0
    while x < width:
        plank_width = 14 + (plank * 5) % 7
        draw.rectangle((x, wall_bottom + 2, x + plank_width - 1, height - 1), fill=hex_rgba(PLANKS[plank % len(PLANKS)]))
        draw.line((x, wall_bottom + 2, x, height - 1), fill=c("plank_gap"))
        for y in range(wall_bottom + 4, height - 3, 7):
            grain_x = x + 2 + (y * 3 + plank * 5) % max(1, plank_width - 4)
            draw.line((grain_x, y, grain_x, y + 2), fill=c("grain"))
            draw.point((grain_x + 1, y + 4), fill=c("grain_light"))
        x += plank_width
        plank += 1
    # Cream wall, lit a little from above
    draw.rectangle((0, 15, width - 1, wall_bottom), fill=c("wall"))
    draw.line((0, 16, width - 1, 16), fill=c("wall_dark"))
    for y in range(18, wall_bottom, 3):
        for x in range(3 + (y % 2) * 5, width - 3, 11):
            draw.point((x, y), fill=c("wall_light"))
    draw.line((0, wall_bottom + 1, width - 1, wall_bottom + 1), fill=c("trim"))
    # Green menu board along the top, for the title
    draw.rectangle((0, 0, width - 1, 14), fill=c("board"))
    draw.line((3, 3, width - 4, 3), fill=c("board_light"))
    draw.line((3, 13, width - 4, 13), fill=c("board_dark"))
    # A copper rail under the board
    draw.line((1, 14, width - 2, 14), fill=c("copper"))
    draw.line((1, 15, width - 2, 15), fill=c("copper_dark"))
    # Outline with rounded corners and an inner bevel
    draw.rectangle((0, 0, width - 1, height - 1), outline=c("frame"))
    draw.line((1, 1, width - 2, 1), fill=c("board_light"))
    draw.line((1, 1, 1, 13), fill=c("board_light"))
    draw.line((1, height - 2, width - 2, height - 2), fill=c("trim"))
    draw.line((width - 2, 2, width - 2, height - 2), fill=c("trim"))
    for cx, cy, sx, sy in ((0, 0, 1, 1), (width - 1, 0, -1, 1), (0, height - 1, 1, -1), (width - 1, height - 1, -1, -1)):
        for i, j in ((0, 0), (1, 0), (0, 1)):
            img.putpixel((cx + sx * i, cy + sy * j), (0, 0, 0, 0))
        img.putpixel((cx + sx, cy + sy), c("frame"))

    # Copper pendant lamps hanging over the counter, either side of the roaster
    for lamp_x in lamps:
        draw.line((lamp_x, 15, lamp_x, 25), fill=c("cord"))
        draw.line((lamp_x - 1, 26, lamp_x + 1, 26), fill=c("copper_dark"))
        draw.line((lamp_x - 2, 27, lamp_x + 2, 27), fill=c("copper"))
        draw.rectangle((lamp_x - 3, 28, lamp_x + 3, 29), fill=c("copper"))
        draw.point((lamp_x - 2, 27), fill=c("copper_light"))
        draw.line((lamp_x - 3, 28, lamp_x - 2, 28), fill=c("copper_light"))
        draw.line((lamp_x - 4, 30, lamp_x + 4, 30), fill=c("copper_dark"))
        # A warm pool of light beneath
        for y in range(31, 38):
            spread = (y - 30) // 2 + 2
            for x in range(lamp_x - spread, lamp_x + spread + 1):
                if (x + y) % 2 == 0 or y < 33:
                    img.putpixel((x, y), c("glow"))
    return img


def player_inventory_slots(draw, top):
    for row in range(3):
        for column in range(9):
            gui_slot(draw, 7 + column * 18, top + row * 18)
    for column in range(9):
        gui_slot(draw, 7 + column * 18, top + 58)


def flame_sprite(img, x0, y0):
    """The burning flame, drawn over the empty one as fuel burns: a yellow heart, orange body and red-orange rim."""
    for y, row in enumerate(FLAME):
        for x, ch in enumerate(row):
            if ch == "#":
                edge = y == 0 or row[x - 1] != "#" or x + 1 >= len(row) or row[x + 1] != "#" or FLAME[y - 1][x] != "#"
                heart = 7 <= y <= 11 and 4 <= x <= 9
                img.putpixel((x0 + x, y0 + y), hex_rgba(GUI["flame_base" if edge else "flame_tip" if heart else "flame"]))


MACHINE_WALL_BOTTOM = 77   # the machine's interface is 176 tall, 10 more than the roaster's


def down_arrow_mask(x, y):
    """A 16x17 arrow pointing down."""
    if y < 9:
        return 5 <= x <= 10
    return abs(x + 0.5 - 8) <= 17 - y


def draw_ghost(img, rows, x0, y0, colour):
    """A faint silhouette in an empty slot, showing what goes there."""
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x0 + x, y0 + y), colour)


GHOST_BUCKET = [
    "................",
    "................",
    "....########....",
    "...#........#...",
    "..#..........#..",
    "..############..",
    "..############..",
    "...##########...",
    "...##########...",
    "...##########...",
    "....########....",
    "....########....",
    "....########....",
    ".....######.....",
    "................",
    "................",
]


GHOST_BOTTLE = [
    "................",
    "......####......",
    "......####......",
    ".......##.......",
    ".......##.......",
    "......####......",
    ".....######.....",
    "....########....",
    "....########....",
    "....########....",
    "....########....",
    "....########....",
    "....########....",
    ".....######.....",
    "................",
    "................",
]


def ghost_of(rows):
    """Any 16x16 icon map as a silhouette: every coloured pixel becomes part of the shape."""
    return ["".join("." if ch == "." else "#" for ch in row) for row in rows]


def right_arrow_mask(x, y):
    """A 16x10 arrow pointing right, for the Grinder Attachment."""
    if x < 9:
        return 3 <= y <= 6
    return abs(y + 0.5 - 5) <= 16 - x


def machine_gui(grinder=False, frother=False, compactor=False):
    """
    The Coffee Machine: water, flame and fuel on the left; grounds, a downward arrow and the cup on the right.
    With the Grinder Attachment, a bean slot with an arrow into the grounds; with the Milk Frother, a milk slot in the top
    right corner, where the right lamp would hang; with the Syrup Compactor, a syrup slot in the bottom left corner,
    diagonally opposite the milk. The screen picks the one that fits.
    """
    img = cafe_panel(176, MACHINE_WALL_BOTTOM, (24,) if frother else (24, 151))
    draw = ImageDraw.Draw(img)
    ghost = hex_rgba("#987a60")
    gui_slot(draw, 41, 16)
    draw_ghost(img, GHOST_BUCKET, 42, 17, ghost)
    gui_slot(draw, 41, 52)
    gui_slot(draw, 109, 16)
    draw_ghost(img, ghost_of(scatter_grounds_rows()), 110, 17, ghost)
    # The cup comes out into a slot framed in copper
    draw.rectangle((107, 54, 128, 75), outline=hex_rgba(GUI["copper_dark"]))
    draw.rectangle((108, 55, 127, 74), outline=hex_rgba(GUI["copper"]))
    gui_slot(draw, 109, 56)
    draw_ghost(img, ghost_of(CUP), 110, 57, ghost)
    player_inventory_slots(draw, 93)
    # The empty flame and arrow, and the full ones the screen draws over them
    for y, row in enumerate(FLAME):
        for x, ch in enumerate(row):
            if ch == "#":
                img.putpixel((42 + x, 36 + y), hex_rgba(GUI["ghost"]))
    flame_sprite(img, 176, 0)
    for y in range(17):
        for x in range(16):
            if down_arrow_mask(x, y):
                img.putpixel((110 + x, 36 + y), hex_rgba(GUI["ghost"]))
                img.putpixel((176 + x, 14 + y), hex_rgba(ARROW_ROAST[y * len(ARROW_ROAST) // 17]))
    # The Grinder Attachment's arrow filling up, drawn over the empty one
    for y in range(10):
        for x in range(16):
            if right_arrow_mask(x, y):
                img.putpixel((194 + x, 42 + y), hex_rgba(ARROW_ROAST[x * len(ARROW_ROAST) // 16]))
    if grinder:
        # The bean slot with a bean silhouette, and its empty arrow into the grounds
        gui_slot(draw, 64, 16)
        draw_ghost(img, ghost_of(scatter_beans()), 65, 17, ghost)
        for y in range(10):
            for x in range(16):
                if right_arrow_mask(x, y):
                    img.putpixel((87 + x, 20 + y), hex_rgba(GUI["ghost"]))
    if frother:
        # The milk slot, with a bucket silhouette
        gui_slot(draw, 145, 16)
        draw_ghost(img, GHOST_BUCKET, 146, 17, ghost)
    if compactor:
        # The syrup slot, with a bottle silhouette, level with the cup
        gui_slot(draw, 13, 56)
        draw_ghost(img, GHOST_BOTTLE, 14, 57, ghost)
    return img


def jei_brewing_panel():
    """
    JEI's Coffee Brewing page, 84x68: the machine's two columns in miniature, on the wall over the counter.
    BrewingCategory uses the same positions.
    """
    width, height = 84, 68
    # The milk slot, shown only on recipes with milk, sits beside the panel
    img = Image.new("RGBA", (width + 18, height))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, width - 1, height - 1), fill=hex_rgba(GUI["wall"]))
    for y in range(1, 62, 3):
        for x in range(3 + (y % 2) * 5, width - 3, 11):
            draw.point((x, y), fill=hex_rgba(GUI["wall_light"]))
    draw.line((0, 63, width - 1, 63), fill=hex_rgba(GUI["trim"]))
    x, plank = 0, 0
    while x < width:
        plank_width = 14 + (plank * 5) % 7
        draw.rectangle((x, 64, x + plank_width - 1, height - 1), fill=hex_rgba(PLANKS[plank % len(PLANKS)]))
        draw.line((x, 64, x, height - 1), fill=hex_rgba(GUI["plank_gap"]))
        x += plank_width
        plank += 1
    gui_slot(draw, 2, 2)
    for y, row in enumerate(FLAME):
        for x, ch in enumerate(row):
            if ch == "#":
                img.putpixel((4 + x, 22 + y), hex_rgba(GUI["ghost"]))
    gui_slot(draw, 40, 2)
    for y in range(17):
        for x in range(16):
            if down_arrow_mask(x, y):
                img.putpixel((41 + x, 21 + y), hex_rgba(GUI["ghost"]))
    draw.rectangle((38, 40, 59, 61), outline=hex_rgba(GUI["copper_dark"]))
    draw.rectangle((39, 41, 58, 60), outline=hex_rgba(GUI["copper"]))
    gui_slot(draw, 40, 42)
    gui_slot(draw, width, 0)
    return img


# The Triple Axis Siphon's touches of gold, after the gold trim on its marble
GOLD = {
    "gold_dark": "#8a6a1c",
    "gold": "#d4a531",
    "gold_light": "#f3d27a",
}
# Red to violet, for the siphon's arrows and the Rainbow Blend
RAINBOW = ["#e04848", "#f08c2e", "#f2cf3a", "#55b04a", "#3b82d6", "#8a4fc4"]
# Each colour of the Rainbow Blend in four shades (shine, base, shadow, edge), like a roast's
RAINBOW_SHADES = [
    ("#ff8a7a", "#e2453c", "#b8302a", "#6e1a16"),
    ("#ffc27a", "#f08c2e", "#c86d1c", "#7a3f10"),
    ("#fff08a", "#f2cf3a", "#c9a820", "#7a6410"),
    ("#9ee08e", "#55b04a", "#3d8a35", "#21501d"),
    ("#94c4ff", "#3b82d6", "#2a62a8", "#163a66"),
    ("#cfa0f0", "#8a4fc4", "#6a3a9a", "#3c1f5c"),
]


def rainbow_blend():
    """
    The grounds heap in the colours of the rainbow, in bands that follow the curve of the heap like a rainbow's arc:
    red along the top, violet at the heart.
    """
    roles = ("#000001", "#000002", "#000003", "#000004")
    heap = grounds_pile(roles)
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        column = [y for y in range(16) if heap.getpixel((x, y))[3]]
        if not column:
            continue
        top = column[0]
        for y in column:
            role = roles.index("#%02x%02x%02x" % heap.getpixel((x, y))[:3])
            band = min(len(RAINBOW_SHADES) - 1, (y - top) * len(RAINBOW_SHADES) // 9)
            img.putpixel((x, y), hex_rgba(RAINBOW_SHADES[band][role]))
    return img


def rainbow_rows(rows, first, last, base_chars, shadow_chars):
    """
    Paints rows first to last of a cup map in the rainbow, red at the top to violet at the bottom: each base_chars
    pixel becomes a digit for its band's colour, and each shadow_chars pixel a letter U to Z for its band's shadow.
    """
    painted = []
    for y, row in enumerate(rows):
        if first <= y <= last:
            band = (y - first) * len(RAINBOW_SHADES) // (last - first + 1)
            row = "".join(str(band) if ch in base_chars else "UVWXYZ"[band] if ch in shadow_chars else ch for ch in row)
        painted.append(row)
    return painted


def rainbow_colours():
    colours = {}
    for band, (shine, base, shadow, edge) in enumerate(RAINBOW_SHADES):
        colours[str(band)] = base
        colours["UVWXYZ"[band]] = shadow
    return colours


def gods_cup():
    """God's Coffee: the takeaway cup with a gold lid and a sleeve striped in the rainbow, for its Rainbow Blend."""
    colours = dict(CUP_COLOURS) | rainbow_colours() | {
        "a": "#fbe39a",  # lid, lit
        "b": "#e0b03c",  # lid
        "h": "#6b4a12",  # sip hole
        "r": "#a97c1c",  # lid rim
    }
    return draw_cup(rainbow_rows(CUP, 7, 12, "CS", "cs"), colours)


def gods_iced_cup():
    """God's Ice Coffee: the clear iced cup with a gold lid and straw, and rainbow coffee around the ice."""
    colours = dict(ICED_CUP_COLOURS) | rainbow_colours() | {
        "G": "#fbe39a",  # straw, lit
        "g": "#d4a531",  # straw
        "L": "#fbe39a",  # lid, lit
        "l": "#e0b03c",  # lid
        "r": "#a97c1c",  # lid rim
        "s": "#8a6a1c",  # the straw, seen through the coffee
    }
    return draw_cup(rainbow_rows(ICED_CUP, 4, 14, "k", "K"), colours)


def gold_frame(draw, x, y):
    """A 22x22 gold frame around a slot, for what the siphon makes. x and y are the frame's corner."""
    draw.rectangle((x, y, x + 21, y + 21), outline=hex_rgba(GOLD["gold_dark"]))
    draw.rectangle((x + 1, y + 1, x + 20, y + 20), outline=hex_rgba(GOLD["gold"]))
    draw.line((x + 1, y + 1, x + 5, y + 1), fill=hex_rgba(GOLD["gold_light"]))
    draw.line((x + 1, y + 1, x + 1, y + 5), fill=hex_rgba(GOLD["gold_light"]))
    gui_slot(draw, x + 2, y + 2)


def siphon_arrows(img, top, left, right, colour):
    """The siphon's three arrows into the middle: down from top, right from left, left from right (each an x, y)."""
    for y in range(17):
        for x in range(16):
            if down_arrow_mask(x, y):
                img.putpixel((top[0] + x, top[1] + y), colour(y, 17))
    for y in range(16):
        for x in range(24):
            if arrow_mask(x, y):
                img.putpixel((left[0] + x, left[1] + y), colour(x, 24))
            if arrow_mask(23 - x, y):
                img.putpixel((right[0] + x, right[1] + y), colour(23 - x, 24))


def siphon_gui():
    """
    The Triple Axis Siphon, laid out like the user's sketch: an input slot on the left, on top and on the right, each with
    an arrow into the gold-framed slot in the middle. The lamps hang over the side slots. Beside it, the three arrows
    filled in the colours of the rainbow, which the screen draws over the empty ones as a blend comes together.
    """
    img = cafe_panel(176, MACHINE_WALL_BOTTOM, (34, 142))
    draw = ImageDraw.Draw(img)
    ghost = hex_rgba("#987a60")
    grounds = ghost_of(scatter_grounds_rows())
    for x, y in ((25, 56), (79, 18), (133, 56)):
        gui_slot(draw, x, y)
        draw_ghost(img, grounds, x + 1, y + 1, ghost)
    gold_frame(draw, 77, 54)
    player_inventory_slots(draw, 93)
    siphon_arrows(img, (80, 37), (48, 57), (104, 57), lambda step, length: hex_rgba(GUI["ghost"]))
    siphon_arrows(img, (176, 0), (176, 17), (176, 33), lambda step, length: hex_rgba(RAINBOW[step * len(RAINBOW) // length]))
    return img


def jei_siphoning_panel():
    """
    JEI's Triple Axis Siphon page, 118x66: the siphon's three slots, arrows and gold-framed result in miniature, on the
    wall over the counter. SiphoningCategory uses the same positions.
    """
    width, height = 118, 66
    img = Image.new("RGBA", (width, height))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, width - 1, height - 1), fill=hex_rgba(GUI["wall"]))
    for y in range(1, 60, 3):
        for x in range(3 + (y % 2) * 5, width - 3, 11):
            draw.point((x, y), fill=hex_rgba(GUI["wall_light"]))
    draw.line((0, 61, width - 1, 61), fill=hex_rgba(GUI["trim"]))
    x, plank = 0, 0
    while x < width:
        plank_width = 14 + (plank * 5) % 7
        draw.rectangle((x, 62, x + plank_width - 1, height - 1), fill=hex_rgba(PLANKS[plank % len(PLANKS)]))
        draw.line((x, 62, x, height - 1), fill=hex_rgba(GUI["plank_gap"]))
        x += plank_width
        plank += 1
    gui_slot(draw, 0, 40)
    gui_slot(draw, 50, 0)
    gui_slot(draw, 100, 40)
    gold_frame(draw, 48, 38)
    siphon_arrows(img, (51, 19), (21, 41), (73, 41), lambda step, length: hex_rgba(GUI["ghost"]))
    return img


# --- the guide book ---

# A leather-bound book with gold thread, gold corners and green tabs, with kraft-paper pages
LEATHER = {
    "outline": "#1c120a",
    "dark": "#3e2515",
    "base": "#5c3820",
    "light": "#6c4428",
    "shine": "#8a5634",
    "thread": "#c9a56a",
}
PAPER = {
    "base": "#f2e6cc",
    "light": "#f8efdc",
    "speck": "#e6d6b6",
    "edge": "#bfa77f",
    "edge_light": "#e9dcbf",
    "edge_mid": "#d2bf98",
    "top_shadow": "#e3d3b2",
    "gutter": ["#cdb68c", "#d8c49e", "#e2d1b0", "#ebdec3"],
    "fold": "#8a6a48",
    "fold_light": "#a5835e",
    "stitch": "#5a3a22",
    "stitch_light": "#7a5636",
}
GREEN = {"outline": "#123522", "dark": "#1d4f32", "base": "#2f7d4f", "light": "#4fa36d"}
# Where the guide's green index tabs stick out of the right-hand pages
GUIDE_TABS = (20, 46, 72, 98)

GUIDE_BOOK_ICON_COLOURS = {
    "o": "#2a170b",  # outline
    "L": "#6e4329",  # leather
    "l": "#4e2d18",  # leather in shadow
    "H": "#8a5634",  # leather, lit
    "S": "#3e2414",  # spine
    "P": "#f2e6cc",  # pages
    "p": "#cdb892",  # page lines
    "Y": "#e0b03c",  # gold
    "y": "#8a6a1c",  # gold in shadow
    "G": "#4fa36d",  # green tab
    "g": "#2f7d4f",  # green tab in shadow
}
GUIDE_BOOK_ICON = [
    "................",
    ".ooooooooooooo..",
    ".oSHHHHHHHHYPo..",
    ".oSHLLLLLLLLPGg.",
    ".oSHLLLYYLLLpGg.",
    ".oSHLLYYyYLLPo..",
    ".oSHLLYyYYLLpo..",
    ".oSHLLYYyYLLPo..",
    ".oSHLLYyYYLLpGg.",
    ".oSHLLLYYLLLPGg.",
    ".oSHLLLLLLLLpo..",
    ".oSlllllllllPo..",
    ".oSllllllllYpo..",
    ".ooooooooooooo..",
    "........Gg......",
    "........g.......",
]


# The guide's buttons and icons, each in the spot Patchouli takes it from. '#' is the colour, '+' its shade; the
# outline is added round the shape. Patchouli shows the highlighted copy, to the right of each, under the mouse.
PAGE_ARROW = [
    "..................",
    "..........#.......",
    "..........##......",
    ".############.....",
    ".##############...",
    ".++++++++++++++...",
    ".+++++++++++++....",
    "..........++......",
    "..........+.......",
    "..................",
]
BACK_ARROW = [
    "..................",
    "....#.............",
    "...##..........##.",
    "..##############+.",
    ".###############+.",
    "..+++++++++++++++.",
    "...++.............",
    "....+.............",
    "..................",
]
SMALL_ARROW = [
    "#....",
    "##...",
    "###..",
    "####.",
    "+++..",
    "++...",
    "+....",
]
PENCIL = [
    "...........",
    "........##.",
    ".......###.",
    "......###..",
    ".....###...",
    "....###....",
    "...###.....",
    "..+##......",
    ".++#.......",
    ".+.........",
    "...........",
]
RESIZE = [
    "...........",
    ".....#.....",
    "....###....",
    "...#####...",
    "....###....",
    "...........",
    "....+++....",
    "...+++++...",
    "....+++....",
    ".....+.....",
    "...........",
]
GEAR = [
    "...........",
    ".#..###..#.",
    "..#######..",
    "..##+++##..",
    ".###+.+###.",
    ".##+...+##.",
    ".###+.+###.",
    "..##+++##..",
    "..#######..",
    ".#..###..#.",
    "...........",
]
STAR = [
    "...........",
    ".....#.....",
    "....###....",
    ".#########.",
    "..#######..",
    "...#####...",
    "...##+##...",
    "..##+.+##..",
    "..#+...+#..",
    "...........",
    "...........",
]
EYE = [
    "...........",
    "...........",
    "...#####...",
    "..#+++++#..",
    ".#++###++#.",
    ".#++#.#++#.",
    ".#++###++#.",
    "..#+++++#..",
    "...#####...",
    "...........",
    "...........",
]
HOURGLASS = [
    "...........",
    "..#######..",
    "...#+++#...",
    "....#+#....",
    ".....#.....",
    ".....#.....",
    "....#.#....",
    "...#.+.#...",
    "..#+++++#..",
    "..#######..",
    "...........",
]
BADGE = [
    "..####..",
    ".######.",
    "########",
    "########",
    "########",
    "########",
    ".######.",
    "..####..",
]
# The marks on the unread, locked and finished badges
BADGE_MARKS = [
    ["...##...", "...##...", "...##...", "...##...", "........", "...##...", "........", "........"],
    ["..###...", ".....#..", "....#...", "...#....", "........", "...#....", "........", "........"],
    ["........", "......#.", ".....##.", ".#..##..", "..###...", "...#....", "........", "........"],
]
ICON_COLOURS = {"#": "#f4ead8", "+": "#c9b48d", "outline": "#3e2515"}
HOVER_COLOURS = {"#": "#f3d27a", "+": "#c08f1f", "outline": "#3e2515"}


def draw_icon(img, rows, x0, y0, colours, outline=True):
    """One button icon at x0, y0, with an outline round it (inside its cell) so it shows on leather and paper alike."""
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x0 + x, y0 + y), hex_rgba(colours[ch]))
    if not outline:
        return
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "." and any(0 <= y + dy < len(rows) and 0 <= x + dx < len(row) and rows[y + dy][x + dx] != "."
                                 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                img.putpixel((x0 + x, y0 + y), hex_rgba(colours["outline"]))


def mirrored(rows):
    return [row[::-1] for row in rows]


def guide_book_gui():
    """
    The guide open on its pages, in the layout Patchouli reads a book texture in (512x256), all drawn here: the open
    book is the 272x180 at the top left, and the ribbon its name is written on is the 140x31 below it. Leather covers
    with gold thread stitched round the edge and gold corners, kraft-paper pages stitched at the spine, and green index
    tabs sticking out of the right-hand pages. Beside them, the book's buttons in cream (gold under the mouse), its
    green bookmarks, the search bar, the separator under titles, the unread/locked/finished badges and the frame
    pictures are shown in.
    """
    img = Image.new("RGBA", (512, 256))
    draw = ImageDraw.Draw(img)
    width, height = 272, 180

    def leather(name):
        return hex_rgba(LEATHER[name])

    def paper(name):
        return hex_rgba(PAPER[name])

    # The leather, mottled a little
    for y in range(height):
        for x in range(width):
            grain = (x * 37 + y * 91 + (x * y) % 13) % 23
            img.putpixel((x, y), leather("light" if grain < 3 else "dark" if grain > 20 else "base"))
    draw.rectangle((0, 0, width - 1, height - 1), outline=leather("outline"))
    draw.line((1, 1, width - 2, 1), fill=leather("shine"))
    draw.line((1, 1, 1, height - 2), fill=leather("shine"))
    draw.line((2, height - 2, width - 2, height - 2), fill=leather("dark"))
    draw.line((width - 2, 2, width - 2, height - 2), fill=leather("dark"))
    # Gold thread stitched round the edge
    for x in range(8, width - 8):
        if x // 3 % 2 == 0:
            img.putpixel((x, 3), leather("thread"))
            img.putpixel((x, height - 4), leather("thread"))
    for y in range(8, height - 8):
        if y // 3 % 2 == 0:
            img.putpixel((3, y), leather("thread"))
            img.putpixel((width - 4, y), leather("thread"))

    # Kraft-paper pages, darker into the gutter, with the edges of the pages beneath showing
    for x0, x1, gutter_x, outer_x, step in ((8, 132, 132, 8, 1), (135, 263, 135, 263, -1)):
        for y in range(7, 171):
            for x in range(x0, x1 + 1):
                grain = (x * 53 + y * 17 + (x ^ y) * 7) % 41
                colour = paper("speck" if grain == 0 else "light" if grain < 3 else "base")
                into_gutter = abs(x - gutter_x)
                if into_gutter < 8:
                    colour = hex_rgba(PAPER["gutter"][into_gutter // 2])
                img.putpixel((x, y), colour)
        # Page edges along the outside and the bottom
        for i, name in enumerate(("edge", "edge_light", "edge_mid")):
            draw.line((outer_x + step * i, 7, outer_x + step * i, 170), fill=paper(name))
        for i, name in enumerate(("edge", "edge_light", "edge_mid")):
            draw.line((min(x0, x1), 170 - i, max(x0, x1), 170 - i), fill=paper(name))
        draw.line((min(x0, x1), 7, max(x0, x1), 7), fill=paper("top_shadow"))
    # The fold, stitched down the middle
    draw.line((133, 7, 133, 170), fill=paper("fold"))
    draw.line((134, 7, 134, 170), fill=paper("fold_light"))
    for y in range(12, 166, 10):
        draw.line((133, y, 133, y + 3), fill=paper("stitch"))
        draw.line((134, y + 1, 134, y + 2), fill=paper("stitch_light"))

    # Gold corners, inside the rounded edge
    gold, gold_dark, gold_light = hex_rgba(GOLD["gold"]), hex_rgba(GOLD["gold_dark"]), hex_rgba(GOLD["gold_light"])
    for i in range(10):
        for j in range(10 - i):
            colour = gold_dark if i + j >= 8 else gold_light if i + j <= 2 else gold
            for x, y in ((i, j), (width - 1 - i, j), (i, height - 1 - j), (width - 1 - i, height - 1 - j)):
                img.putpixel((x, y), colour)
    # Rounded outer corners
    for cx, cy, sx, sy in ((0, 0, 1, 1), (width - 1, 0, -1, 1), (0, height - 1, 1, -1), (width - 1, height - 1, -1, -1)):
        for i, j in ((0, 0), (1, 0), (0, 1)):
            img.putpixel((cx + sx * i, cy + sy * j), (0, 0, 0, 0))
        img.putpixel((cx + sx, cy + sy), gold_dark)

    # Green index tabs coming out from between the right-hand pages, over the leather
    green = {name: hex_rgba(value) for name, value in GREEN.items()}
    for top in GUIDE_TABS:
        draw.rectangle((261, top, 270, top + 15), fill=green["base"])
        draw.line((261, top, 269, top), fill=green["outline"])
        draw.line((261, top + 15, 269, top + 15), fill=green["outline"])
        draw.line((270, top + 1, 270, top + 14), fill=green["outline"])
        draw.line((261, top + 1, 268, top + 1), fill=green["light"])
        draw.line((261, top + 14, 269, top + 14), fill=green["dark"])
        draw.line((269, top + 2, 269, top + 13), fill=green["dark"])
        # A shadow on the leather under each tab
        draw.line((264, top + 16, 269, top + 16), fill=leather("outline"))

    # The ribbon the book's name goes on: pointed at the right, folding back round the book's edge at the left
    for y in range(31):
        for x in range(140):
            if y <= 22:
                inside = x <= 131 + (11 - abs(y - 11)) * 8 // 11
            else:
                inside = y - 23 <= x <= 7
            if not inside:
                continue
            img.putpixel((x, 180 + y), green["base"])
    ribbon = [[img.getpixel((x, 180 + y))[3] > 0 for x in range(140)] for y in range(31)]
    for y in range(31):
        for x in range(140):
            if not ribbon[y][x]:
                continue
            edge = any(not (0 <= x + dx < 140 and 0 <= y + dy < 31 and ribbon[y + dy][x + dx])
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            name = "outline" if edge else "dark" if y > 20 else "light" if y < 3 else "base"
            img.putpixel((x, 180 + y), green[name])

    # The buttons, each with its highlighted copy to its right
    for rows, x, y in ((PAGE_ARROW, 272, 0), (mirrored(PAGE_ARROW), 272, 10), (BACK_ARROW, 308, 0)):
        draw_icon(img, rows, x, y, ICON_COLOURS)
        draw_icon(img, rows, x + len(rows[0]), y, HOVER_COLOURS)
    for rows, y in ((SMALL_ARROW, 20), (mirrored(SMALL_ARROW), 27)):
        draw_icon(img, rows, 272, y, ICON_COLOURS, outline=False)
        draw_icon(img, rows, 277, y, HOVER_COLOURS, outline=False)
    for rows, x, y in ((PENCIL, 308, 9), (RESIZE, 330, 9), (GEAR, 308, 20), (STAR, 330, 20), (EYE, 308, 31),
                       (HOURGLASS, 330, 31)):
        draw_icon(img, rows, x, y, ICON_COLOURS)
        draw_icon(img, rows, x + 11, y, HOVER_COLOURS)

    # Bookmarks, sticking out to the right of the book: a bookmark, and the tab with a plus for adding one; the
    # highlighted ones are lighter
    for u, light in ((272, False), (285, True)):
        for v, plus in ((160, False), (170, True)):
            for y in range(10):
                right = 12 if 2 <= y <= 7 else 11 if y in (1, 8) else 10
                for x in range(right + 1):
                    edge = x == right or y in (0, 9)
                    name = "outline" if edge else "light" if y == 1 or (light and y < 5) else "dark" if y >= 7 else "base"
                    img.putpixel((u + x, v + y), green[name])
            if plus:
                draw.line((u + 6, v + 2, u + 6, v + 7), fill=hex_rgba(ICON_COLOURS["#"]))
                draw.line((u + 4, v + 4, u + 8, v + 4), fill=hex_rgba(ICON_COLOURS["#"]))
                draw.line((u + 4, v + 5, u + 8, v + 5), fill=hex_rgba(ICON_COLOURS["+"]))

    # The line under titles, with a coffee bean in the middle
    ink = hex_rgba("#5a3a22")[:3]
    for x in range(110):
        if not 50 <= x <= 59:
            img.putpixel((140 + x, 181), ink + (110,))
    for y, row in enumerate([".####.", "##.###", ".####."]):
        for x, ch in enumerate(row):
            if ch == "#":
                img.putpixel((140 + 52 + x, 180 + y), ink + (200,))

    # The search bar, with a magnifying glass
    bar = (140, 183, 238, 196)
    draw.rectangle(bar, fill=hex_rgba("#fbf3e2"), outline=hex_rgba("#3e2515"))
    draw.line((141, 195, 237, 195), fill=hex_rgba("#e3d3b2"))
    draw.line((237, 184, 237, 195), fill=hex_rgba("#e3d3b2"))
    draw.ellipse((142, 185, 148, 191), outline=hex_rgba("#5a3a22"))
    draw.line((147, 191, 150, 194), fill=hex_rgba("#5a3a22"))

    # A padlock, for entries that are still locked
    draw.rectangle((257, 183, 260, 186), outline=hex_rgba(GOLD["gold_dark"]))
    draw.rectangle((256, 187, 261, 192), fill=hex_rgba(GOLD["gold"]), outline=hex_rgba(GOLD["gold_dark"]))
    img.putpixel((258, 189), hex_rgba("#3e2515"))
    img.putpixel((259, 189), hex_rgba("#3e2515"))
    img.putpixel((258, 190), hex_rgba("#3e2515"))

    # Badges: unread (red), locked (amber) and finished (green)
    for i, (colour, mark) in enumerate(zip(("#b3322c", "#d98a1c", "#3b8a2e"), BADGE_MARKS)):
        for y, row in enumerate(BADGE):
            for x, ch in enumerate(row):
                if ch == "#":
                    edge = x in (0, 7) or y in (0, 7) or row[x - 1] == "." or row[x + 1:x + 2] == "."
                    shade = "#2b170c" if edge else colour
                    img.putpixel((140 + i * 8 + x, 197 + y), hex_rgba(shade))
                if mark[y][x] == "#":
                    img.putpixel((140 + i * 8 + x, 197 + y), hex_rgba("#fbf3e2"))

    # The frame pictures are shown in
    for inset, alpha in ((0, 120), (1, 60)):
        draw.rectangle((405 + inset, 149 + inset, 510 - inset, 254 - inset), outline=ink + (alpha,))
    return img


def guide_page_filler():
    """
    What Patchouli prints on an empty page, 128x128: a faint takeaway cup with a few beans, like a stamp in the
    paper.
    """
    img = Image.new("RGBA", (128, 128))
    ink = hex_rgba("#7a5636")[:3]
    for y, row in enumerate(CUP):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            alpha = 150 if ch in "or" else 70
            for dy in range(4):
                for dx in range(4):
                    img.putpixel((32 + x * 4 + dx, 20 + y * 4 + dy), ink + (alpha,))
    for left, top in ((22, 92), (60, 98), (96, 90)):
        for y, row in enumerate(BEAN):
            for x, ch in enumerate(row):
                if ch != ".":
                    alpha = 150 if ch == "d" else 80
                    for dy in range(2):
                        for dx in range(2):
                            img.putpixel((left + x * 2 + dx, top + y * 2 + dy), ink + (alpha,))
    return img


def roaster_gui():
    img = cafe_panel(166, WALL_BOTTOM)
    draw = ImageDraw.Draw(img)

    def c(name):
        return hex_rgba(GUI[name])

    def slot(x, y, size=18):
        gui_slot(draw, x, y, size)

    slot(55, 16)
    slot(55, 52)
    # The roasted beans come out into a bigger slot framed in copper
    copper_slot(draw, 109, 28)
    for row in range(3):
        for column in range(9):
            slot(7 + column * 18, 83 + row * 18)
    for column in range(9):
        slot(7 + column * 18, 141)

    # The empty flame and arrow on the wall, and the full ones the screen draws over them
    ghost_flame_and_arrow(img, 56, 36, 79, 34)
    flame_sprite(img, 176, 0)
    for y in range(16):
        for x in range(24):
            if arrow_mask(x, y):
                img.putpixel((176 + x, 14 + y), hex_rgba(ARROW_ROAST[x * len(ARROW_ROAST) // 24]))
    return img


def draw_cup(rows=CUP, colours=CUP_COLOURS):
    img = Image.new("RGBA", (16, 16))
    assert len(rows) == 16
    for y, row in enumerate(rows):
        assert len(row) == 16, row
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), hex_rgba(colours[ch]))
    return img


def textures():
    yield "item/raw_coffee_beans", draw_map(scatter_beans())
    for roast, shades in ROASTS.items():
        yield f"item/{roast}_coffee_beans", draw_map(scatter_beans(), shades)
        yield f"item/{roast}_coffee_grounds", grounds_pile(shades)
    yield "item/rainbow_blend", rainbow_blend()
    yield "item/coffee", draw_cup()
    yield "item/ice_coffee", draw_cup(ICED_CUP, ICED_CUP_COLOURS)
    yield "item/gods_coffee", gods_cup()
    yield "item/gods_ice_coffee", gods_iced_cup()
    yield "item/guide_book", draw_cup(GUIDE_BOOK_ICON, GUIDE_BOOK_ICON_COLOURS)
    yield "gui/container/coffee_roaster", roaster_gui()
    yield "gui/container/coffee_machine", machine_gui()
    yield "gui/container/coffee_machine_grinder", machine_gui(grinder=True)
    yield "gui/container/coffee_machine_frother", machine_gui(frother=True)
    yield "gui/container/coffee_machine_grinder_frother", machine_gui(grinder=True, frother=True)
    yield "gui/container/coffee_machine_compactor", machine_gui(compactor=True)
    yield "gui/container/coffee_machine_grinder_compactor", machine_gui(grinder=True, compactor=True)
    yield "gui/container/coffee_machine_frother_compactor", machine_gui(frother=True, compactor=True)
    yield "gui/container/coffee_machine_grinder_frother_compactor", machine_gui(grinder=True, frother=True, compactor=True)
    yield "gui/jei/coffee_roasting", jei_roasting_panel()
    yield "gui/jei/coffee_grinding", jei_grinding_panel()
    yield "gui/jei/coffee_brewing", jei_brewing_panel()
    yield "gui/container/triple_axis_siphon", siphon_gui()
    yield "gui/jei/siphoning", jei_siphoning_panel()
    yield "gui/book/guide", guide_book_gui()
    yield "gui/book/filler", guide_page_filler()
    yield "block/coffee_bush_stage0", draw_map(STARTER_BUSH)
    yield "block/coffee_bush_stage1", draw_map(BUSH)
    yield "block/coffee_bush_stage2", draw_map(with_cherries(BUSH, UNRIPE))
    yield "block/coffee_bush_stage3", draw_map(with_cherries(BUSH, RIPE))


def save_preview(images, path, scale=12):
    """The 16x16 textures side by side, enlarged. The interface is left out; open its texture to see it."""
    images = [img for img in images if img.size == (16, 16)]
    sheet = Image.new("RGBA", (len(images) * (16 * scale + 8), 16 * scale), (120, 130, 140, 255))
    for i, img in enumerate(images):
        big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
        sheet.alpha_composite(big, (i * (16 * scale + 8), 0))
    sheet.save(path)


def main():
    drawn = []
    for name, img in textures():
        drawn.append(img)
        path = TEXTURES / f"{name}.png"
        if path.exists():
            print(f"kept     {name} (delete it to redraw)")
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path)
        print(f"created  {name}")
    if "--preview" in sys.argv:
        save_preview(drawn, sys.argv[sys.argv.index("--preview") + 1])


if __name__ == "__main__":
    main()

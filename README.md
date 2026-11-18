# Nasug-Go

Local business locator and nearest-open-business suggestions for Nasugbu Poblacion.
Kotlin, XML views, Fragments, RecyclerView, MapLibre map with free OpenFreeMap tiles, local JSON data (Gson).



## Map

- Engine: [MapLibre Native](https://github.com/maplibre/maplibre-native) (`org.maplibre.gl:android-sdk:11.8.0`)
- Tiles/styles: free public [OpenFreeMap](https://openfreemap.org) styles. Dark = `fiord`, light = `positron`
  (see `DARK_STYLES` / `LIGHT_STYLES` at the bottom of `MapFragment.kt`; each list falls back to the next entry if
  a style fails to load).
- Pins are a GeoJSON symbol layer drawn by `MarkerIcons.kt`; the blue dot is a circle layer from the device location.
- The map credit (OpenFreeMap / OpenMapTiles / OpenStreetMap) is shown by the map's "i" button. Keep it visible,
  it is required by the tile providers.
- OpenFreeMap is a donation-funded public service with no guarantee of uptime. If the app grows, switch the style
  URLs to a paid or self-hosted provider; nothing else changes.



## Features

- **Explore tab:** search, category chips, "Nearest open" suggestion card (with a Map shortcut), category-coloured
  rows with distance and open/closed status.
- **Map tab:** dark/light map (layers button), category pins (greyed out when closed, larger when selected),
  floating search and chips, draggable bottom sheet (list, or details of the tapped pin with Navigate/Details),
  nearest-open and my-location buttons. The camera is limited to the Nasugbu area.
- **Detail screen:** header with category icon and status, about, address and phone, weekly hours with today
  highlighted, offerings, bottom Navigate and Call bar.

## Data

`app/src/main/assets/businesses.json` 

| Field | Notes |
|-------|-------|
| `id` | unique string |
| `category` | detailed type, used for pin colour, icon and the row label. Chips show the three groups in `CategoryGroups.kt`: Food & Beverages (Dining), Retail (Groceries, Shopping, Hardware, Pharmacies), Services (Beauty & Fitness, Entertainment, Services, Health). Other categories show only under All |
| `latitude`, `longitude` | decimal degrees |
| `hours` | `[]` = not listed; otherwise one entry per day (`MON`..`SUN`), `open`/`close` as `HH:mm`; `null` = closed all day; `"24:00"` close = round the clock |
| `phone` | `null` if unknown |
| `keywords` | hidden search words (botika, kape, gupit...) |
| `barangay` | e.g. `Barangay 3`; searchable |

## Code map (object-oriented design)

- `data/Business` : abstract base class (immutable). Subclasses `FoodBusiness`, `RetailBusiness`, `ServiceBusiness`,
  `GeneralBusiness` set the `group` and their own hidden search words (inheritance + polymorphism).
  `BusinessFactory` picks the subclass from the category.
- `data/OpeningHours` : encapsulates the weekly schedule and the open/closed rules.
- `data/BusinessDto` : plain Gson record for one JSON entry; `data/BusinessGroup` : enum of the three groups.
- `data/BusinessFilter` : immutable search text + group, with the `accepts(business)` rule.
- `data/BusinessSource` (interface) and `JsonBusinessSource` : where businesses come from; `BusinessRepository` shares one.
- `data/SearchTerms` (composition): a Business HAS-A SearchTerms for its extra search words. `data/BarangayNames`: numeric barangay ordering. `ui/SortOption`: sort strategies (Nearest, Barangay, Name).
- `data/LocationUtils` : distance helper.
- `ui/SharedViewModel` : filter, location, nearest suggestion, shared by both tabs.
- `ui/HomeFragment`, `ui/MapFragment`, `ui/BusinessDetailActivity` : the three screens.
- `ui/MarkerIcons`, `ui/CategoryStyle` : pin drawing and category colours/icons.
- Device location uses Google Play services' fused location provider (no key needed).

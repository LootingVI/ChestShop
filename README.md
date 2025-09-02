# ChestShop Plugin for Paper 1.21.5

**Developed by Flori** - A professional, feature-rich ChestShop plugin for Paper 1.21.5 with advanced configuration options, comprehensive statistics, item trading system, and powerful admin tools.

## 🌟 Key Features Overview

- **💰 Traditional Money-Based Shops** - Buy/sell items with economy integration
- **🔄 Revolutionary Item Trading System** - Trade items directly without money
- **✨ Advanced Hologram System** - Beautiful floating displays with auto-initialization
- **🔒 Complete Shop Protection** - Multi-layer security against griefing
- **📊 Professional Statistics** - Comprehensive analytics and tracking
- **🔍 Smart Search System** - Find shops by item, owner, or price range
- **⚙️ Robust Admin Tools** - Maintenance mode, backups, and bulk operations
- **🎯 Thread-Safe Performance** - Optimized for high-performance servers

## Core Features

### 🏪 Shop Management
- ✅ **Easy Shop Creation** - Create shops with simple commands
- ✅ **Flexible Trading** - Buy-only, sell-only, or buy/sell shops
- ✅ **Smart Shop Detection** - Automatic chest and sign linking
- ✅ **Shop Toggle** - Enable/disable shops without removal
- ✅ **Dynamic Pricing** - Update shop prices on-the-fly
- ✅ **Shop Refilling** - Easy restocking system for shop owners
- ✅ **Auto-Save System** - Automatic data persistence

### 🔒 Advanced Protection
- ✅ **Shop Protection** - Complete protection against griefing
- ✅ **Hopper Protection** - Prevents item theft via hoppers/dispensers
- ✅ **Explosion Protection** - Shields shops from TNT and creepers
- ✅ **Piston Protection** - Prevents shop displacement
- ✅ **Nearby Block Protection** - Blocks dangerous items near shops
- ✅ **Owner Access Control** - Configurable chest access for owners

### 📊 Statistics & Analytics
- ✅ **Comprehensive Statistics** - Track all shop activities
- ✅ **Player Analytics** - Individual player performance metrics
- ✅ **Server-Wide Stats** - Global shop economy overview
- ✅ **Transaction Tracking** - Detailed buy/sell history
- ✅ **Popular Items Analysis** - Trending item detection
- ✅ **Profit Analysis** - Earnings and spending calculations
- ✅ **Performance Metrics** - Shop efficiency ratings

### 🔍 Advanced Search System
- ✅ **Multi-Type Search** - Search by item, owner, or price range
- ✅ **Distance-Based Results** - Find nearby shops
- ✅ **Best Price Finder** - Locate best deals automatically
- ✅ **Smart Filtering** - Only active, stocked shops
- ✅ **Configurable Limits** - Customizable result counts

### 🎯 Professional Admin Tools
- ✅ **Maintenance Mode** - Server-wide shop disable for maintenance
- ✅ **Backup System** - Complete shop data backups with timestamps
- ✅ **Data Reset Tools** - Reset individual or all player data
- ✅ **Shop Cleanup** - Remove invalid/broken shops
- ✅ **Bulk Operations** - Remove all shops from specific players
- ✅ **Hot Configuration Reload** - No server restart required

### 🌐 Integration & Compatibility
- ✅ **Vault Integration** - Full economy system support
- ✅ **Multi-World Support** - Works across all server worlds
- ✅ **Paper 1.21.5 Optimized** - Modern API usage
- ✅ **Thread-Safe Operations** - Async-friendly design
- ✅ **Memory Efficient** - Minimal resource usage

### 🎨 Visual & User Experience
- ✅ **Holographic Displays** - Floating item and text displays
- ✅ **Dynamic Signs** - Auto-updating shop information
- ✅ **Professional Messaging** - Standardized message system
- ✅ **German Localization** - Complete German language support
- ✅ **Smart Notifications** - Low stock and full shop alerts
- ✅ **Interactive Previews** - Shift-click shop information

## Installation

1. **Install Dependencies:**
   - Paper 1.21.5+ Server
   - Vault Plugin
   - An Economy Plugin (e.g., EssentialsX)

2. **Compile Plugin:**
   ```bash
   mvn clean package
   ```

3. **Install Plugin:**
   - Copy the generated `.jar` file from `target/` to your `plugins/` folder
   - Restart the server

## Commands

### Basic Player Commands
- `/chestshop create <item> <amount> <buy-price> [sell-price]` - Create a new shop
- `/chestshop remove` - Remove a shop (look at chest or sign)
- `/chestshop info` - Display detailed shop information
- `/chestshop list [player]` - List shops (own or specific player if admin)
- `/chestshop toggle` - Enable/disable a shop
- `/chestshop refill [amount]` - Refill shop with items from inventory
- `/chestshop price <buy-price> <sell-price>` - Update shop prices
- `/chestshop stats [player|global]` - View statistics
- `/chestshop search <type> <parameters>` - Search for shops
- `/chestshop help` - Display help information

### Advanced Search Commands
- `/chestshop search item <MATERIAL>` - Find shops selling specific items
- `/chestshop search owner <player>` - Find shops owned by specific player
- `/chestshop search price <min> <max> <buy|sell>` - Find shops within price range

### 🔄 Item Trading System Commands
- `/chestshop item create <buy_item> <buy_amount> <sell_item> <sell_amount>` - Create item trading shop
- `/chestshop item convert <buy_item> <buy_amount> <sell_item> <sell_amount>` - Convert normal shop to item trading
- `/chestshop item info` - Show information about an item trading shop
- `/chestshop item list [player]` - List all item trading shops
- `/chestshop item update <buy_item> <buy_amount> <sell_item> <sell_amount>` - Update an item trading shop
- `/chestshop item help` - Display item trading help

### Debug Commands (Admin Only)
- `/chestshop debug shop` - Debug shop at your current location
- `/chestshop debug reload` - Force reload all shops
- `/chestshop debug save` - Force save all shops
- `/chestshop debug list` - List all shops with debug information

### Admin Commands
- `/chestshop admin removeall <player>` - Remove all shops from a player
- `/chestshop admin holograms <reload|remove>` - Manage holographic displays
- `/chestshop admin cleanup` - Remove invalid/broken shops
- `/chestshop admin stats` - View comprehensive server statistics
- `/chestshop admin reset <player>` - Reset player statistics
- `/chestshop admin maintenance` - Toggle maintenance mode
- `/chestshop admin backup` - Create complete data backup
- `/chestshop reload` - Reload configuration files

### Command Aliases
The plugin supports the following command aliases:
- `/cs` (short form)
- `/shop`
- `/cshop`

## Shop Creation Guide

### Step-by-Step Setup
1. **Place Chest** - Place a chest at your desired shop location
2. **Place Sign** - Place a sign adjacent to the chest (next to, above, or below)
3. **Create Shop** - Look at the chest and execute the creation command:
   ```
   /chestshop create DIAMOND 1 100 90
   ```
   This creates a shop for 1 diamond with buy price 100 and sell price 90

### Traditional Shop Types (Money-Based)
- **Buy & Sell Shop**: `/chestshop create IRON_INGOT 16 50 45` (Players buy for 50, sell for 45)
- **Buy-Only Shop**: `/chestshop create EMERALD 1 100 0` (Players can only buy)
- **Sell-Only Shop**: `/chestshop create COBBLESTONE 64 0 5` (Players can only sell)

### 🔄 Item Trading Shop Types (No Money Required)
- **Item Trading Shop**: `/chestshop item create DIAMOND 1 OAK_LOG 16` (Trade 1 Diamond for 16 Oak Logs)
- **Resource Exchange**: `/chestshop item create IRON_INGOT 8 COAL 16` (Trade 8 Iron for 16 Coal)
- **Item Conversion**: `/chestshop item create RAW_GOLD 4 GOLD_INGOT 4` (Trade 4 Raw Gold for 4 Gold Ingots)
- **Convert Existing**: `/chestshop item convert EMERALD 1 DIAMOND 1` (Convert existing shop to item trading)

## Shop Interaction

### For Customers
- **Left Click** on chest or sign = Buy items from shop
- **Right Click** on chest or sign = Sell items to shop
- **Shift + Click** = Display detailed shop information and preview

### For Shop Owners
- **Normal Click** - Opens chest if `owner-free-access` is enabled
- **Refill Shop** - Use `/chestshop refill` to restock from inventory
- **Update Prices** - Use `/chestshop price <buy> <sell>` to adjust pricing
- **Toggle Shop** - Use `/chestshop toggle` to enable/disable temporarily

## Configuration System

### config.yml - Main Configuration
Complete configuration with all available options:

```yaml
general:
  debug: false                    # Enable debug logging
  max-shops-per-player: 10       # Shop limit per player (0 = unlimited)
  allowed-worlds: []             # Restrict to specific worlds (empty = all)
  auto-save-interval: 5          # Auto-save frequency in minutes
  default-language: "en"         # Default language (en/de)

shop:
  price-limits:
    min-buy-price: 0.01          # Minimum buy price
    max-buy-price: 1000000.0     # Maximum buy price
    min-sell-price: 0.01         # Minimum sell price
    max-sell-price: 1000000.0    # Maximum sell price
  creation:
    creation-cost: 100.0         # Cost to create a shop
    allowed-items: []            # Whitelist items (empty = all allowed)
    banned-items:                # Blacklist dangerous items
      - "BEDROCK"
      - "COMMAND_BLOCK"
      - "STRUCTURE_BLOCK"
      - "BARRIER"
  behavior:
    owner-free-access: true      # Owners can access their chests
    auto-disable-when-empty: false
    max-transaction-amount: 64
    auto-update-signs: true

signs:
  format:
    line1: "&9[ChestShop]"       # Sign formatting with color codes
    line2: "&b%owner%"
    line3: "&a%amount% %item%"
    line4: "&eB: %buy% S: %sell%"
  colors:
    active: "&a"                 # Color for active shops
    inactive: "&c"               # Color for inactive shops
    out-of-stock: "&6"          # Color for empty shops
    out-of-space: "&6"          # Color for full shops

holograms:
  enabled: true                  # Enable holographic displays
  show-item: true               # Show floating item above shop
  show-text: true               # Show shop information text
  show-prices: true             # Show price information
  text-format: "&6[ChestShop] &b%owner%"
  price-format: "&aBuy: %buy% &cSell: %sell%"
  height-offset: 1.5            # Height above chest
  update-interval: 20           # Update frequency in ticks

protection:
  enabled: true                 # Enable all protection features
  radius: 1                     # Protection radius in blocks
  only-owner-break: true        # Only owners can break shop blocks
  hopper-protection: true       # Prevent hopper item theft
  allow-owner-inventory-access: true
  protect-nearby-blocks: true   # Prevent dangerous blocks nearby
  explosion-protection: true    # Protect from explosions
  piston-protection: true       # Prevent piston displacement

economy:
  transaction-fee: 0.0          # Fee percentage on transactions
  fee-to-server: true           # Send fees to server (true) or owner (false)
  shop-tax: 0.0                 # Tax on shop sales
  min-owner-balance: 0.0        # Minimum balance required

notifications:
  low-stock:
    enabled: true               # Alert owners about low stock
    threshold: 5                # Stock level for alerts
  full-shop:
    enabled: true               # Alert when shop is full
  periodic-check:
    enabled: false              # Regular shop health checks
    interval-minutes: 30
  login-notifications:
    enabled: true               # Show status on player login
    show-summary: true

statistics:
  enabled: true                 # Track all statistics
  track-players: true           # Individual player metrics
  save-interval: 10             # Save frequency in minutes

search:
  max-results: 10               # Maximum search results
  distance-based: true          # Sort by distance
  max-distance: 1000           # Search radius limit
  show-distance: true           # Display distances

logging:
  log-transactions: true        # Log all buy/sell transactions
  log-shop-creation: true       # Log shop creation/deletion
  log-file: "chestshop.log"    # Log file name
  log-admin-actions: true       # Log admin commands
```

### messages.yml - Message System
All user-facing messages are fully customizable and localized. The plugin uses a standardized message system where every player interaction references predefined message keys, making translation and customization easy.

### shops.yml - Shop Data Storage
Automatically generated file containing all shop data with backup functionality.

## Permissions System

### Player Permissions
```yaml
# Basic usage permissions (default: true)
chestshop.use                    # Basic plugin usage
chestshop.create                 # Create shops
chestshop.remove                 # Remove own shops
chestshop.info                   # View shop information
chestshop.list                   # List own shops
chestshop.toggle                 # Toggle own shops
chestshop.refill                 # Refill own shops
chestshop.price                  # Update shop prices
chestshop.stats                  # View own statistics
chestshop.search                 # Search for shops
chestshop.buy                    # Buy from shops
chestshop.sell                   # Sell to shops

# Advanced permissions
chestshop.create.unlimited       # Bypass shop limits
chestshop.create.free           # Create shops without cost
chestshop.create.banned-items   # Use banned items in shops
chestshop.create.anywhere       # Create in restricted worlds
```

### Admin Permissions
```yaml
chestshop.admin.*               # All admin permissions
chestshop.admin.removeall       # Remove any player's shops
chestshop.admin.cleanup         # Clean up invalid shops
chestshop.admin.stats           # View server statistics
chestshop.admin.reload          # Reload configuration
chestshop.admin.holograms       # Manage holograms
chestshop.admin.reset           # Reset player data
chestshop.admin.maintenance     # Toggle maintenance mode
chestshop.admin.backup          # Create/restore backups
chestshop.list.others          # List other players' shops
chestshop.stats.others         # View other players' stats
chestshop.stats.server         # View server-wide statistics

# Protection bypass permissions
chestshop.protection.bypass     # Bypass all protections
chestshop.protection.access-all # Access any shop chest
chestshop.protection.break-any  # Break any shop blocks
chestshop.protection.place-anywhere # Place blocks near shops

# Economy bypass permissions
chestshop.economy.no-fee       # Bypass transaction fees
chestshop.economy.no-tax       # Bypass shop taxes
chestshop.economy.unlimited    # Bypass balance requirements
chestshop.buy.free            # Buy items without cost
chestshop.sell.overprice      # Bypass sell price limits
```

## Advanced Features

### Search System
The plugin includes a powerful search system allowing players and admins to find specific shops:

**Search by Item:**
```bash
/chestshop search item DIAMOND
```

**Search by Owner:**
```bash
/chestshop search owner Steve
```

**Search by Price Range:**
```bash
/chestshop search price 50 100 buy    # Find buy prices between 50-100
/chestshop search price 10 50 sell    # Find sell prices between 10-50
```

### Statistics System
Comprehensive statistics tracking for both players and administrators:

**Player Statistics Include:**
- Total shops owned (active/inactive)
- Total transactions completed
- Money earned from sales
- Money spent on purchases
- Most popular selling items
- Shop efficiency ratings
- Low stock and full shop counts

**Server Statistics Include:**
- Total shops across server
- Active vs inactive shop ratios
- Most popular traded items
- Top earning players
- Total economic activity
- Average shop age and performance

### 🔄 Item Trading System
A revolutionary shop type that allows direct item-for-item exchanges without requiring money:

- **Economy-Free Trading** - Exchange items directly without any currency
- **Custom Exchange Rates** - Set your own item trading ratios
- **Clear Feedback** - Informative messages for insufficient stock or space
- **Special Signs** - Unique sign formatting for item trading shops
- **Dedicated Commands** - Full command set for managing item shops
- **Stock Protection** - Same security as money-based shops

**Trading Interface:**
- Left-click to see trade preview
- Right-click to execute trade
- Shift-click for detailed information

### Holographic Displays
Advanced visual enhancements for shops:
- **Floating Items** - Visual representation of shop contents
- **Information Text** - Owner and shop details
- **Price Displays** - Buy/sell prices clearly visible
- **Status Indicators** - Active, inactive, out of stock states
- **Customizable Formats** - Fully configurable appearance
- **Auto-Initialization** - Hologram system loads after worlds are ready

### Admin Management Tools

**Maintenance Mode:**
- Temporarily disable all shop interactions
- Allows server maintenance without disrupting shop data
- Automatic player notifications

**Backup System:**
- Create timestamped JSON backups of all shop data
- Includes shop configurations, statistics, and metadata
- Easy restore functionality for disaster recovery

**Data Management:**
- Reset individual player statistics
- Bulk remove shops from problematic players
- Clean up orphaned or invalid shops
- Real-time configuration reloading

### Protection Features

**Comprehensive Shop Security:**
- Prevent unauthorized block breaking around shops
- Block hopper/dispenser item theft attempts
- Protect against piston-based shop displacement
- Shield shops from explosion damage
- Restrict placement of dangerous blocks nearby

**Configurable Access Control:**
- Owner-only chest access (configurable)
- Admin override capabilities
- Granular permission system
- Bypass permissions for trusted players

## Usage Examples

### Basic Shop Examples

**Simple Sell Shop (Player sells to others):**
```bash
/chestshop create STONE 64 0 10
```
Players can buy 64 stone for 10 coins (shop owner sells)

**Buy & Sell Shop (Trading post):**
```bash
/chestshop create DIAMOND 1 120 100
```
Shop buys diamonds for 100, sells for 120 (profit margin)

**Resource Collection Shop (Buy from players):**
```bash
/chestshop create WHEAT 32 15 0
```
Shop buys 32 wheat for 15 coins (collecting resources)

### Advanced Management Examples

**Update Shop Prices:**
```bash
/chestshop price 150 125
```
Updates shop to buy for 125, sell for 150

**Refill Shop Stock:**
```bash
/chestshop refill 128
```
Adds 128 items from inventory to shop

**Search for Best Deals:**
```bash
/chestshop search item DIAMOND
/chestshop search price 50 100 buy
```

## Technical Architecture

### Performance Optimizations
- **Thread-Safe Operations** - All shop operations use concurrent data structures
- **Async-Friendly Design** - Non-blocking database operations
- **Efficient Caching** - Smart memory management for frequently accessed data
- **Minimal Resource Usage** - Optimized for high-performance servers
- **Auto-Save System** - Prevents data loss with configurable intervals

### Modern Development Practices
- **Clean Code Architecture** - Well-organized, maintainable codebase
- **Standardized Messaging** - No hardcoded strings in user interface
- **Comprehensive Error Handling** - Graceful failure recovery
- **Extensive Configuration** - Every feature is configurable
- **Professional Logging** - Detailed operation tracking

### Development Information
- **Java 21+** - Latest Java features and performance
- **Maven Build System** - Industry-standard build process
- **Paper API 1.21.5** - Latest Minecraft server features
- **Vault Economy API** - Universal economy integration
- **Modern Plugin Architecture** - Bootstrap-based loading system

## Troubleshooting

### Common Issues
1. **Vault Not Found** - Ensure Vault and economy plugins are installed
2. **Permission Denied** - Check permission configuration
3. **Shop Not Created** - Verify chest and sign placement
4. **Protection Issues** - Review protection settings in config

### Debug Mode
Enable `debug: true` in config.yml for detailed logging information.

## License

**ChestShop Plugin License** - This plugin can be freely modified and used, but attribution to the original author (Flori) must be maintained. See LICENSE file for complete details.

**Important for Modifications:** Any changes or redistribution must clearly state:
- "Original ChestShop Plugin by Flori"
- What changes were made

## Support & Development

This plugin was specifically developed for Paper 1.21.5 and leverages modern Bukkit/Paper APIs for optimal performance and compatibility.

### Contributing
When contributing or modifying this plugin:
1. Maintain proper attribution to the original author
2. Document any changes made
3. Test thoroughly with Paper 1.21.5
4. Follow existing code patterns and conventions

## Acknowledgments

**Originally developed by Flori** - If you modify or use this plugin, please maintain proper attribution to the original author!

---

*This plugin represents a complete, professional ChestShop solution with enterprise-grade features and German localization.*

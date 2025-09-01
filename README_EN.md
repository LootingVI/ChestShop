# ChestShop Plugin for Paper 1.21.5

**Developed by Flori** - A user-friendly ChestShop plugin for Paper 1.21.5 with extensive configuration options and professional message management.

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

### Admin Commands
- `/chestshop admin removeall <player>` - Remove all shops from a player
- `/chestshop admin holograms <reload|remove>` - Manage holographic displays
- `/chestshop admin cleanup` - Remove invalid/broken shops
- `/chestshop admin stats` - View comprehensive server statistics
- `/chestshop admin reset <player>` - Reset player statistics
- `/chestshop admin maintenance` - Toggle maintenance mode
- `/chestshop admin backup` - Create complete data backup
- `/chestshop reload` - Reload configuration files

### Aliases
The plugin supports the following command aliases:
- `/cs` (short form)
- `/shop`
- `/cshop`

## Shop Creation

1. **Place Chest** - Place a chest at the desired location
2. **Place Sign** - Place a sign next to, above, or below the chest
3. **Create Shop** - Look at the chest and execute the creation command:
   ```
   /chestshop create DIAMOND 1 100 90
   ```
   This creates a shop for 1 diamond with buy price 100 and sell price 90

## Shop Usage

### For Customers:
- **Left Click** on chest or sign = Buy items
- **Right Click** on chest or sign = Sell items
- **Shift + Click** = Display shop information

### For Shop Owners:
- Normal clicks open the chest (if `owner-free-access` is enabled)
- `/chestshop toggle` to enable/disable
- `/chestshop remove` to delete

## Configuration

### config.yml
Main configuration file with all settings:

```yaml
general:
  max-shops-per-player: 10  # Max shops per player
  allowed-worlds: []        # Allowed worlds (empty = all)
  auto-save-interval: 5     # Auto-save in minutes

shop:
  price-limits:
    min-buy-price: 0.01
    max-buy-price: 1000000.0
  creation:
    creation-cost: 100.0    # Cost for shop creation
    banned-items:           # Banned items
      - "BEDROCK"
      - "COMMAND_BLOCK"

protection:
  enabled: true             # Enable shop protection
  only-owner-break: true    # Only owner can break
  hopper-protection: true   # Hopper protection
```

### messages.yml
All messages are fully customizable and localized in German. You can modify this file to change any user-facing text or translate to other languages.

### shops.yml
Automatically generated file containing all shop data.

## Permissions

### Standard Permissions (for all players):
- `chestshop.use` - Basic usage
- `chestshop.create` - Create shops
- `chestshop.remove` - Remove own shops
- `chestshop.info` - Display shop info
- `chestshop.list` - List own shops
- `chestshop.toggle` - Enable/disable own shops

### Admin Permissions:
- `chestshop.admin` - All admin rights
- `chestshop.reload` - Reload configuration
- `chestshop.remove.others` - Remove other players' shops
- `chestshop.list.others` - List other players' shops
- `chestshop.bypass.*` - Various bypass rights

## Examples

### Simple Sell Shop:
```bash
/chestshop create STONE 64 0 10
```
Sells 64 stone for 10 (sell only, no buying)

### Buy and Sell Shop:
```bash
/chestshop create DIAMOND 1 120 100
```
Buys diamonds for 120, sells for 100

### Buy-Only Shop:
```bash
/chestshop create BREAD 16 5 0
```
Sells 16 bread for 5 (sell to players only)

## Admin Features

### Shop Statistics
The plugin provides comprehensive statistics for server administrators:
- **Low Stock Analysis** - Monitor shops running low on inventory
- **Full Shop Detection** - Track shops with full chests
- **Stock Overview** - Total item counts across all shops
- **Profit Analysis** - Net profit calculations and trends

### Maintenance Mode
Administrators can enable maintenance mode to temporarily disable all shop interactions while performing server maintenance.

### Data Management
- **Backup System** - Create backups of all shop data
- **Reset Functionality** - Reset all shop data when needed
- **Hot Reload** - Reload configuration without server restart

## Technical Details

- **Thread-Safe**: All shop operations are thread-safe
- **Async-Friendly**: Supports asynchronous operations
- **Performance-Optimized**: Efficient data structures and caching
- **Memory-Efficient**: Minimal RAM usage
- **Auto-Save**: Automatic saving prevents data loss
- **Standardized Messaging**: All user-facing messages use the centralized message system

## Development & Support

This plugin was specifically developed for Paper 1.21.5 and uses modern Bukkit/Paper APIs.

### Developer Information:
- Java 21+
- Maven Build System
- Paper API 1.21.5
- Vault Economy API

### Code Quality:
- Professional message management system
- No hardcoded strings in user-facing messages
- Comprehensive configuration options
- Clean, maintainable codebase

## License

**ChestShop Plugin License** - This plugin can be freely modified and used, but attribution to the original author (Flori) must be maintained. See LICENSE file for complete details.

**Important for Modifications:** Any changes or redistribution must clearly state:
- "Original ChestShop Plugin by Flori"
- What changes were made

## Acknowledgments

**Originally developed by Flori** - If you modify or use this plugin, don't forget to give attribution to the original author!

---

*For German documentation, see README.md*

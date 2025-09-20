# AGENTS.md - System Monitoring Dashboard

## Project Overview

This is a **real-time system monitoring dashboard** built with Vue.js that displays server/device health information including CPU usage, memory, disk space, and network statistics for both Windows and Linux systems.

### Core Technologies
- **Frontend Framework**: Vue.js (CDN version)
- **UI Components**: Element UI (for progress circles and components)
- **Communication**: WebSocket for real-time data updates
- **Languages**: HTML, JavaScript, CSS
- **Architecture**: Single-page application (SPA) with real-time data visualization

### Project Purpose
- Monitor multiple servers/devices in real-time
- Display system metrics (CPU, memory, disk, network) 
- Support both Windows and Linux platforms
- Provide geographic IP information for devices
- Show network interface details with IPv4/IPv6 support

## File Structure

### Core Files
- `simple-list.html` - Main application file containing HTML structure, CSS styles, and Vue.js logic
- `css/ele.css` - Element UI component styles (minified)
- `css/reset.css` - CSS reset styles (Meyer's reset v5.0.1)
- `js/vue.js` - Vue.js framework (CDN version)
- `js/ele.js` - Element UI JavaScript components
- `js/axios.js` - HTTP client library (currently unused but available)
- `js/tools.js` - Empty utility file (placeholder)

### Assets
- `img/linux.jpg` - Linux system icon
- `img/win.webp` - Windows system icon

## Development Guidelines

### Code Structure Patterns
- **Single File Application**: All Vue.js code is embedded in `simple-list.html`
- **Inline Styles**: CSS is primarily defined within `<style>` tags in the HTML
- **WebSocket Integration**: Real-time data updates via WebSocket connection
- **Component-based UI**: Uses Element UI progress circles and other components

### Data Structure Conventions
The application expects device data in the following format:
```javascript
{
  cpuInfo: { cpuUsage, cpuCount, cpuModel },
  memInfo: { totalMemMb, usedMemMb, freeMemMb, usedMemPercentage },
  driveInfo: { totalGb, usedGb, freeGb, usedPercentage },
  netstatInfo: { total: { inputMb, outputMb }, [interface]: { inputMb, outputMb } },
  osInfo: { type, platform, release, ip, hostname, arch, uptime },
  ipInfo: { country, regionName, city, isp, query },
  netInterface: { [interfaceName]: [{ address, netmask, family, mac, cidr }] }
}
```

### CSS Conventions
- **Color Scheme**: Dark theme with blue-gray palette
  - Background: `#222e42`
  - Cards: `#2b384f` 
  - Card titles: `#324b75`
  - Text: White (`#ffffff`)
- **Card-based Layout**: All content sections use `.card` class
- **Responsive Design**: Flexbox-based layout for device information

### JavaScript Patterns
- **Vue.js Options API**: Uses traditional Vue 2 syntax with `data`, `methods`, `mounted`
- **WebSocket Lifecycle**: Connection management in Vue lifecycle hooks
- **Data Formatting**: Utility methods for time, memory, and network speed formatting
- **Status Color Coding**: Progressive color changes based on usage percentages
  - Normal: `#45a0ff` (blue)
  - Warning (70-90%): `#dfa238` (orange) 
  - Critical (>90%): `#f34c4a` (red)

## Best Practices

### Development Workflow
- Test WebSocket connections with appropriate server endpoints
- Ensure device data matches expected JSON structure
- Validate responsive design across different screen sizes
- Test with both Windows and Linux device data

### Performance Considerations
- WebSocket data updates should be throttled to prevent UI lag
- Large device lists should be paginated or virtualized
- Consider lazy loading for network interface details
- Optimize image assets (convert to WebP where supported)

### Maintainability
- Keep Vue.js component logic modular and focused
- Extract repeated CSS patterns into reusable classes
- Document WebSocket message format and API contract
- Consider migrating inline styles to external CSS files for larger projects

### Browser Compatibility
- Uses modern JavaScript features (const, let, arrow functions)
- Relies on WebSocket support (IE10+)
- Element UI requires modern browser support
- Consider polyfills for older browser support if needed

## Configuration

### WebSocket Connection
Update the WebSocket URL in the script section:
```javascript
const ws = new WebSocket("ws://服务端地址:服务器端口?token=你的token&type=view&endpointName=web");
```

### Data Dictionary
The `dict` object maps internal property names to display labels (currently in Chinese). Modify this for internationalization.

### IP Filtering
The application filters devices by IP ranges (currently `10.0.8.*`). Update the filtering logic in the `wsMessage` method for different network ranges.

## Common Issues & Solutions

### WebSocket Connection Problems
- Verify server endpoint is accessible
- Check authentication token validity
- Ensure CORS policies allow WebSocket connections
- Monitor browser network tab for connection errors

### Data Display Issues
- Validate incoming JSON structure matches expected format
- Check for null/undefined values in device data
- Verify data type conversions (strings vs numbers)
- Test with edge cases (very high/low values)

### Performance Issues
- Monitor Vue.js reactivity with large datasets
- Consider using `Object.freeze()` for static data
- Implement virtual scrolling for many devices
- Debounce rapid WebSocket updates

## Future Improvements

### Recommended Enhancements
- Migrate to Vue 3 Composition API for better performance
- Add TypeScript for better type safety
- Implement data persistence (localStorage/IndexedDB)
- Add filtering and sorting capabilities
- Create responsive mobile layout
- Add dark/light theme toggle
- Implement chart history and trending
- Add alert/notification system for critical thresholds

### Code Organization
- Split into separate `.vue` components
- Extract WebSocket logic into a service
- Create a proper build system (Vite/Webpack)
- Add unit tests for data formatting functions
- Implement proper error boundaries and loading states
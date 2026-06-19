const fs = require('fs');
const path = require('path');

const srcSkillsDir = path.join(__dirname, '../../skills-registry/src/main/resources/skills');
const destPublicDir = path.join(__dirname, '../public');
const destRegistryJson = path.join(destPublicDir, 'registry.json');
const destSkillsDir = path.join(destPublicDir, 'skills');

// Helper to get version from pom.xml
function getVersionFromPom(pomPath) {
    let version = '1.0.0-SNAPSHOT'; // Fallback
    try {
        if (fs.existsSync(pomPath)) {
            const content = fs.readFileSync(pomPath, 'utf8');
            // Isolate project header before dependencies/properties/build blocks
            const projectHeader = content.split(/<(?:dependencies|properties|build|dependencyManagement|profiles)>/)[0];
            // Remove parent tags from header to avoid matching parent version first
            const headerWithoutParent = projectHeader.replace(/<parent>[\s\S]*?<\/parent>/, '');
            const versionMatch = headerWithoutParent.match(/<version>([^<]+)<\/version>/);
            if (versionMatch && versionMatch[1]) {
                version = versionMatch[1].trim();
            } else {
                // Try parent version matching from original content
                const parentVersionMatch = content.match(/<parent>[\s\S]*?<version>([^<]+)<\/version>[\s\S]*?<\/parent>/);
                if (parentVersionMatch && parentVersionMatch[1]) {
                    version = parentVersionMatch[1].trim();
                }
            }
        }
    } catch (e) {
        console.warn(`Could not read pom.xml at ${pomPath}, using fallback version:`, version, e);
    }
    return version;
}

const registryPomPath = path.join(__dirname, '../../skills-registry/pom.xml');
const pluginPomPath = path.join(__dirname, '../../skills-maven-plugin/pom.xml');

const registryVersion = getVersionFromPom(registryPomPath);
const pluginVersion = getVersionFromPom(pluginPomPath);

console.log(`Resolved Registry version: ${registryVersion}`);
console.log(`Resolved Plugin version: ${pluginVersion}`);

// Ensure public directories exist
if (!fs.existsSync(destPublicDir)) {
    fs.mkdirSync(destPublicDir, { recursive: true });
}
if (fs.existsSync(destSkillsDir)) {
    fs.rmSync(destSkillsDir, { recursive: true, force: true });
}
fs.mkdirSync(destSkillsDir, { recursive: true });

// Helper to copy directory recursively
function copyRecursive(src, dest) {
    const stats = fs.statSync(src);
    if (stats.isDirectory()) {
        if (!fs.existsSync(dest)) {
            fs.mkdirSync(dest, { recursive: true });
        }
        fs.readdirSync(src).forEach(child => {
            copyRecursive(path.join(src, child), path.join(dest, child));
        });
    } else {
        fs.copyFileSync(src, dest);
    }
}

// 2. Scan and aggregate skills
const registry = [];

if (fs.existsSync(srcSkillsDir)) {
    const dirs = fs.readdirSync(srcSkillsDir).filter(name => {
        return fs.statSync(path.join(srcSkillsDir, name)).isDirectory();
    });

    for (const dirName of dirs) {
        const skillSrcPath = path.join(srcSkillsDir, dirName);
        const metadataPath = path.join(skillSrcPath, 'metadata.json');

        if (!fs.existsSync(metadataPath)) {
            console.warn(`Skipping ${dirName}: metadata.json not found.`);
            continue;
        }

        const metadataRaw = fs.readFileSync(metadataPath, 'utf8');
        const metadata = JSON.parse(metadataRaw);

        // Strict validation: Only allow 'name', 'description', and 'tags' keys
        const allowedKeys = ['name', 'description', 'tags'];
        const metadataKeys = Object.keys(metadata);
        const invalidKeys = metadataKeys.filter(k => !allowedKeys.includes(k));

        if (invalidKeys.length > 0) {
            throw new Error(`Validation Error in skill "${dirName}": metadata.json contains forbidden keys: ${invalidKeys.join(', ')}. Only 'name', 'description', and 'tags' are allowed.`);
        }

        // Add to registry array
        registry.push({
            id: dirName,
            name: metadata.name,
            description: metadata.description,
            tags: metadata.tags || [],
            version: registryVersion
        });

        // Replicate skill files
        const skillDestPath = path.join(destSkillsDir, dirName);
        copyRecursive(skillSrcPath, skillDestPath);
    }
} else {
    console.warn(`Source skills directory not found: ${srcSkillsDir}`);
}

// 3. Write registry.json
fs.writeFileSync(destRegistryJson, JSON.stringify(registry, null, 2), 'utf8');
console.log(`Successfully aggregated ${registry.length} skills into public/registry.json`);

// 4. Write versions.json
const destVersionsJson = path.join(destPublicDir, 'versions.json');
fs.writeFileSync(destVersionsJson, JSON.stringify({
    registryVersion: registryVersion,
    pluginVersion: pluginVersion
}, null, 2), 'utf8');
console.log(`Successfully wrote versions.json with Registry: ${registryVersion}, Plugin: ${pluginVersion}`);

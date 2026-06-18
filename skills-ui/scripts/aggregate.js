const fs = require('fs');
const path = require('path');

const srcSkillsDir = path.join(__dirname, '../../skills-registry/src/main/resources/skills');
const destPublicDir = path.join(__dirname, '../public');
const destRegistryJson = path.join(destPublicDir, 'registry.json');
const destSkillsDir = path.join(destPublicDir, 'skills');

// 1. Read version from root pom.xml
const rootPomPath = path.join(__dirname, '../../pom.xml');
let version = '1.0.0-SNAPSHOT'; // Fallback
try {
    const rootPomContent = fs.readFileSync(rootPomPath, 'utf8');
    const versionMatch = rootPomContent.match(/<groupId>io\.github\.bksantani<\/groupId>\s*<artifactId>jskillflow<\/artifactId>\s*<version>([^<]+)<\/version>/);
    if (versionMatch && versionMatch[1]) {
        version = versionMatch[1].trim();
    } else {
        const simpleMatch = rootPomContent.match(/<version>([^<]+)<\/version>/);
        if (simpleMatch && simpleMatch[1]) {
            version = simpleMatch[1].trim();
        }
    }
} catch (e) {
    console.warn('Could not read root pom.xml, using fallback version:', version, e);
}

console.log(`Resolved project version: ${version}`);

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
            version: version
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

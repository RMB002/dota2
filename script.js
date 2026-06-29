const https = require('https');
const fs = require('fs');

https.get('https://api.opendota.com/api/heroes', (resp) => {
  let data = '';
  resp.on('data', (chunk) => { data += chunk; });
  resp.on('end', () => {
    const heroes = JSON.parse(data);
    const output = heroes.map(h => {
      let name_formatted = h.name.replace('npc_dota_hero_', '');
      return {
        id: name_formatted,
        name: h.localized_name,
        imageUrl: `https://cdn.cloudflare.steamstatic.com/apps/dota2/images/dota_react/heroes/${name_formatted}.png`,
        lanes: [],
        tacticalTips: [],
        abilities: [],
        roles: []
      };
    });
    fs.writeFileSync('app/src/main/assets/heroes.json', JSON.stringify(output, null, 2));
    console.log(`Saved ${output.length} heroes`);
  });
});

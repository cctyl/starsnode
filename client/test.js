
const getOsData = require('./app/utils/os-data');


(
    async function () {
        let osData = await getOsData();
        console.log(JSON.stringify(osData))
    }
)();

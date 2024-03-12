const osu = require('node-os-utils')
const os = require('os')
const getOsData = require('./app/utils/os-data');


(
    async function () {
        let  result =  await os.networkInterfaces();
        console.log(result)
    }
)();

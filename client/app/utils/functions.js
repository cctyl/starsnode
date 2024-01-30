const axios = require('axios');
module.exports = {

    guid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    },
    async getIpInfo() {
        try {
            let {data} = await axios.get('http://ip-api.com/json?lang=zh-CN');
            console.log('getIpInfo Success: ', new Date());
            return data;
        } catch (error) {
            console.log('getIpInfo Error: ', new Date(), error)
        }
    }
}

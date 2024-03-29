module.exports = function (config) {
    config.set({
        browsers: ['ChromeHeadless'],
        // The directory where the output file lives
        basePath: 'out',
        // The file itself
        files: ['karma-tests.js'],
        frameworks: ['cljs-test'],
        plugins: ['karma-cljs-test', 'karma-chrome-launcher'],
        colors: true,
        logLevel: config.LOG_INFO,
        browserNoActivityTimeout: 60000,
        browserDisconnectTimeout: 60000,
        client: {
            args: ["shadow.test.karma.init"],
            singleRun: true
        }
    })
};

module.exports = {
    entry: "./src/combat_砲撃戦.ts"
    , output: {
        path: '../../script/combat/砲撃戦'
        , filename: "combat_砲撃戦.js"
    }
    , resolve: {
        extensions: ['', '.ts', '.js']
    }
    , module: {
        loaders: [
            { test: /\.ts$/, loader: 'ts-loader' }
        ]
    }
};

# Stock Data Crawler

A Java Swing desktop app that crawls Hong Kong listed stock quotes, stores them in a local MySQL database, and displays them in a sortable table. Use it to screen stocks by P/E, P/B, and dividend yield.

## What it does

- Fetches quote pages for a range of stock codes (for example `1` through `100`)
- Parses each page for name, market cap, P/E, P/B, and yield
- Writes valid rows into a local `stocksdb` MySQL database
- Shows the results in a Swing table you can sort by yield (high to low), P/E (low to high), P/B (low to high), or stock code

## How it works

`Crawler` launches `CrawlerApp`, a Swing window. You enter a start and end stock code, then click **Start Crawling**. The crawl runs on a background thread so the UI stays responsive.

`StockQuoteCrawler` then:

1. Connects to MySQL via `DbConnectionConfig` (`jdbc:mysql://localhost:3306/stocksdb`)
2. Clears the existing `stock` table
3. For each code in the range, requests `STOCK_QUOTE_URL` + the code
4. Parses the HTML with [Jsoup](https://jsoup.org/), reading the quote summary table and company name from the page DOM
5. Skips rows with no usable data (market cap `N/A` and zero P/E, P/B, and yield)
6. Inserts remaining rows into `stock` (`code`, `name`, `marketcap`, `pe`, `pb`, `yield`)
7. Reloads the table in the UI when the crawl finishes

The sort buttons re-query the database rather than sorting in memory:

| Button | Query |
| --- | --- |
| Yield (desc) | non-`N/A` yield, highest first |
| P/E (Asc) | non-`N/A` P/E, lowest first |
| P/B (Asc) | non-`N/A` P/B, lowest first |
| Stock code (default) | all rows, insertion/code order |
| populateTable | reload the full table without crawling again |

## Setup

**Requirements:** Java 8, Gradle, MySQL running locally.

1. Create a database named `stocksdb` and a `stock` table with columns `code`, `name`, `marketcap`, `pe`, `pb`, and `yield`.
2. Set the JDBC user and password in `DbConnectionConfig`.
3. Set `STOCK_QUOTE_URL` in `StockQuoteCrawler` to the quote page base URL (it currently uses a placeholder: `https://example.com/quote?stockCode=`). The crawler appends the numeric stock code.
4. Run the app from the `Crawler` main class, or with Gradle:

```bash
./gradlew run
```

If `run` is not configured, launch `com.squidzoo.stockdatacrawler.Crawler` from your IDE.

## License

MIT. See [LICENSE](LICENSE).

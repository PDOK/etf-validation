name: PDOK ETF TEST

on:
  # Trigger the workflow on push or pull request,
  # but only for the master branch
  push:
    branches:
      - master
  pull_request:
    branches:
      - master
  schedule:
    - cron: "03 03 * * *"

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 1.8
        uses: actions/setup-java@v1
        with:
          java-version: 1.8
      - name: Build with Maven
        run: mvn -B --file pom.xml test
      - name: Deploy
        uses: peaceiris/actions-gh-pages@v3
        if: always()
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./reports

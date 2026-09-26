export interface Config {
  nodeEnv: "development" | "test" | "production";
  port: number;
  seedMode: boolean;
}

export const loadConfig = (source: NodeJS.ProcessEnv = process.env): Config => {
  const nodeEnv = source.NODE_ENV ?? "development";
  if (!["development", "test", "production"].includes(nodeEnv))
    throw new Error("NODE_ENV is invalid");
  const port = Number(source.PORT ?? 3000);
  if (!Number.isInteger(port) || port < 1 || port > 65535)
    throw new Error("PORT is invalid");
  return {
    nodeEnv: nodeEnv as Config["nodeEnv"],
    port,
    seedMode: source.NUST_MOBILITY_SEED_MODE === "true",
  };
};

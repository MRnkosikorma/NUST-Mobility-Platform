import type {
  RouteDto,
  VehicleDto,
  FareDto,
} from "../../../../../packages/contracts/src/index.js";

const devRoutes: RouteDto[] = [
  {
    id: "route_001",
    name: "NUST Main Campus — Bulawayo CBD",
    origin: "NUST Admin Loop",
    destination: "City Hall Terminal",
    defaultFareMinor: 100, // $1.00
  },
  {
    id: "route_002",
    name: "NUST Main Campus — Selbourne Park",
    origin: "NUST Gate 1",
    destination: "Selbourne Park Shops",
    defaultFareMinor: 50, // $0.50
  },
  {
    id: "route_003",
    name: "NUST Main Campus — Hillside",
    origin: "NUST Gate 2",
    destination: "Hillside Shopping Centre",
    defaultFareMinor: 75, // $0.75
  },
];

const devVehicles: VehicleDto[] = [
  {
    id: "veh_001",
    registration: "NUST-BUS-01",
    operatorName: "NUST Campus Shuttle Service",
    capacity: 45,
    assignedRouteId: "route_001",
  },
  {
    id: "veh_002",
    registration: "NUST-BUS-02",
    operatorName: "NUST Campus Shuttle Service",
    capacity: 35,
    assignedRouteId: "route_002",
  },
  {
    id: "veh_003",
    registration: "COMM-BUS-88",
    operatorName: "City Commuter Services",
    capacity: 22,
    assignedRouteId: "route_003",
  },
];

export function getRoutes(): RouteDto[] {
  return devRoutes;
}

export function getRouteById(id: string): RouteDto | undefined {
  return devRoutes.find((r) => r.id === id);
}

export function getVehicles(): VehicleDto[] {
  return devVehicles;
}

export function getVehicleByRegistration(reg: string): VehicleDto | undefined {
  return devVehicles.find(
    (v) => v.registration.toUpperCase() === reg.toUpperCase(),
  );
}

export function getFareForRoute(routeId: string): FareDto {
  const route = getRouteById(routeId) ?? devRoutes[0];
  return {
    id: `fare_${route.id}`,
    routeId: route.id,
    amountMinor: route.defaultFareMinor,
    currency: "USD",
  };
}
